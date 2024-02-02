package dev.shivamnagpal.users.helpers;

import dev.shivamnagpal.users.daos.SessionDao;
import dev.shivamnagpal.users.daos.UserDao;
import dev.shivamnagpal.users.dtos.request.PasswordUpdateRequestDTO;
import dev.shivamnagpal.users.dtos.response.wrapper.ErrorResponse;
import dev.shivamnagpal.users.enums.AccountStatus;
import dev.shivamnagpal.users.enums.ErrorCode;
import dev.shivamnagpal.users.exceptions.RestException;
import dev.shivamnagpal.users.models.User;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.ModelConstants;
import dev.shivamnagpal.users.utils.UtilMethods;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserHelper {
    private final JsonObject config;

    private final UserDao userDao;

    private final SessionDao sessionDao;

    public UserHelper(JsonObject config, UserDao userDao, SessionDao sessionDao) {
        this.config = config;
        this.userDao = userDao;
        this.sessionDao = sessionDao;
    }

    public Future<User> getUserById(SqlConnection sqlConnection, Long userId) {
        return userDao.getById(sqlConnection, userId)
                .compose(
                        userOptional -> userOptional.map(Future::succeededFuture)
                                .orElseGet(
                                        () -> Future.failedFuture(
                                                new RestException(
                                                        HttpResponseStatus.BAD_REQUEST,
                                                        ErrorResponse.from(ErrorCode.USER_NOT_FOUND)
                                                )
                                        )
                                )
                );
    }

    public Future<User> getUserByEmail(SqlConnection sqlConnection, String email) {
        return userDao.getUserByEmail(sqlConnection, email)
                .compose(
                        userOptional -> userOptional.map(Future::succeededFuture)
                                .orElseGet(
                                        () -> Future.failedFuture(
                                                new RestException(
                                                        HttpResponseStatus.BAD_REQUEST,
                                                        ErrorResponse.from(ErrorCode.USER_NOT_FOUND)
                                                )
                                        )
                                )
                );
    }

    public Future<Void> updatePasswordAndLogOutAllSessions(
            SqlConnection sqlConnection,
            MongoClient mongoClient,
            Long userId,
            PasswordUpdateRequestDTO passwordUpdateRequestDTO
    ) {
        return getUserById(sqlConnection, userId)
                .compose(
                        user -> updatePasswordAndLogOutAllSessions(
                                sqlConnection,
                                mongoClient,
                                user,
                                passwordUpdateRequestDTO
                        )
                );
    }

    public Future<Void> updatePasswordAndLogOutAllSessions(
            SqlConnection sqlConnection,
            MongoClient mongoClient,
            User user,
            PasswordUpdateRequestDTO passwordUpdateRequestDTO
    ) {
        if (BCrypt.checkpw(passwordUpdateRequestDTO.getPlainPassword(), user.getPassword())) {
            return Future.failedFuture(
                    new RestException(
                            HttpResponseStatus.NOT_ACCEPTABLE,
                            ErrorResponse.from(ErrorCode.NEW_PASSWORD_CANNOT_BE_SAME_AS_THE_OLD_PASSWORD)
                    )
            );
        }
        return userDao.updatePassword(sqlConnection, user.getId(), passwordUpdateRequestDTO.getHashedPassword())
                .compose(v -> sessionDao.logoutAllSessions(mongoClient, user.getId()));
    }

    public Future<Void> deleteScheduleAccount(Pool pgPool) {
        return UtilMethods.nonBlockingWhile(
                () -> deleteScheduleAccountBatch(pgPool),
                size -> size < config.getInteger(Constants.DELETION_CRON_BATCH_SIZE)
        )
                .compose(s -> Future.succeededFuture());
    }

    private Future<Integer> deleteScheduleAccountBatch(Pool pgPool) {
        return pgPool.withTransaction(
                sqlConnection -> userDao.findAccountsScheduledForDeletion(
                        sqlConnection,
                        config.getInteger(Constants.DELETION_CRON_BATCH_SIZE)
                )
                        .compose(users -> {
                            if (users.isEmpty()) {
                                return Future.succeededFuture(0);
                            }
                            OffsetDateTime currentTime = OffsetDateTime.now();
                            for (User user : users) {
                                user.setAccountStatus(AccountStatus.DELETED);
                                if (user.getMeta() == null) {
                                    user.setMeta(new JsonObject());
                                }
                                user.getMeta().put(ModelConstants.EMAIL, user.getEmail());
                                user.setEmail(UUID.randomUUID().toString());
                                user.setDeleted(true);
                                user.setTimeLastModified(currentTime);
                            }
                            return userDao.updateUserAccountsAsDeleted(sqlConnection, users)
                                    .map(users.size());
                        })
        );
    }
}
