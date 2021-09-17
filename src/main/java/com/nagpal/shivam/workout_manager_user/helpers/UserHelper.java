package com.nagpal.shivam.workout_manager_user.helpers;

import com.nagpal.shivam.workout_manager_user.daos.SessionDao;
import com.nagpal.shivam.workout_manager_user.daos.UserDao;
import com.nagpal.shivam.workout_manager_user.dtos.request.PasswordUpdateRequestDTO;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.sqlclient.SqlConnection;
import org.springframework.security.crypto.bcrypt.BCrypt;


public class UserHelper {
    private final UserDao userDao;
    private final SessionDao sessionDao;

    public UserHelper(UserDao userDao, SessionDao sessionDao) {
        this.userDao = userDao;
        this.sessionDao = sessionDao;
    }

    public Future<User> getUserById(SqlConnection sqlConnection, Long userId) {
        return userDao.getById(sqlConnection, userId)
                .compose(userOptional -> {
                    if (userOptional.isEmpty()) {
                        return Future.failedFuture(
                                new ResponseException(HttpResponseStatus.BAD_REQUEST.code(),
                                        MessageConstants.USER_NOT_FOUND, null)
                        );
                    }
                    return Future.succeededFuture(userOptional.get());
                });
    }

    public Future<User> getUserByEmail(SqlConnection sqlConnection, String email) {
        return userDao.getUserByEmail(sqlConnection, email)
                .compose(userOptional -> {
                    if (userOptional.isEmpty()) {
                        return Future.failedFuture(new ResponseException(HttpResponseStatus.BAD_REQUEST.code(),
                                MessageConstants.USER_NOT_FOUND,
                                null
                        ));
                    }
                    return Future.succeededFuture(userOptional.get());
                });
    }

    public Future<Void> updatePasswordAndLogOutAllSessions(SqlConnection sqlConnection, MongoClient mongoClient,
                                                           Long userId,
                                                           PasswordUpdateRequestDTO passwordUpdateRequestDTO) {
        return getUserById(sqlConnection, userId)
                .compose(user -> {
                    if (BCrypt.checkpw(passwordUpdateRequestDTO.getPlainPassword(), user.getPassword())) {
                        return Future.failedFuture(new ResponseException(HttpResponseStatus.NOT_ACCEPTABLE.code(),
                                MessageConstants.NEW_PASSWORD_CANNOT_BE_SAME_AS_THE_OLD_PASSWORD, null
                        ));
                    }
                    return userDao.updatePassword(sqlConnection, userId, passwordUpdateRequestDTO.getHashedPassword())
                            .compose(v -> sessionDao.logoutAllSessions(mongoClient, userId));
                });
    }

    public Future<Void> resetPasswordAndLogOutAllSessions(SqlConnection sqlConnection, MongoClient mongoClient,
                                                          Long userId,
                                                          PasswordUpdateRequestDTO passwordUpdateRequestDTO) {
        return getUserById(sqlConnection, userId)
                .compose(user -> userDao.updatePassword(sqlConnection, userId,
                                        passwordUpdateRequestDTO.getHashedPassword()
                                )
                                .compose(v -> sessionDao.logoutAllSessions(mongoClient, userId))
                );
    }
}
