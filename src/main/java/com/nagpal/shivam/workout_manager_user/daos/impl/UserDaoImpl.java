package com.nagpal.shivam.workout_manager_user.daos.impl;

import com.nagpal.shivam.workout_manager_user.daos.UserDao;
import com.nagpal.shivam.workout_manager_user.enums.AccountStatus;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.utils.DbUtils;
import com.nagpal.shivam.workout_manager_user.utils.PgExceptionCodes;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Optional;

@NoArgsConstructor
public class UserDaoImpl implements UserDao {
    public static final String INSERT_USER = "INSERT INTO \"user\"" +
            "(first_name, last_name, email, \"password\", email_verified, account_status, meta, deleted, " +
            "time_created, time_last_modified) " +
            "VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10) RETURNING id";

    public static final String ACTIVATE_USER =
            "UPDATE \"user\" SET email_verified=$1, account_status=$2, time_last_modified=$3 WHERE id=$4";

    public static final String SELECT_USER_BY_EMAIL = "SELECT * FROM \"user\" where email=$1";

    @Override
    public Future<Long> signUp(SqlClient sqlClient, User user) {
        Tuple values = Tuple.of(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                user.getEmailVerified(),
                user.getAccountStatus(),
                user.getMeta(),
                user.getDeleted(),
                user.getTimeCreated(),
                user.getTimeLastModified()
        );
        return DbUtils.executeQueryAndReturnOne(sqlClient, INSERT_USER, values, DbUtils::mapRowToId)
                .map(Optional::get)
                .recover(throwable -> {
                    if (throwable instanceof PgException) {
                        PgException pgException = (PgException) throwable;
                        if (pgException.getCode().equals(PgExceptionCodes.UNIQUE_KEY_CONSTRAINT_VIOLATION)) {
                            ResponseException responseException =
                                    new ResponseException(HttpResponseStatus.BAD_REQUEST.code(),
                                            "User with " + pgException.getDetail(), null);
                            return Future.failedFuture(responseException);
                        }
                    }
                    return Future.failedFuture(throwable);
                });
    }

    @Override
    public Future<Void> activateUser(SqlClient sqlClient, Long userId) {
        Tuple values = Tuple.of(
                true,
                AccountStatus.ACTIVE,
                OffsetDateTime.now(),
                userId
        );
        return DbUtils.executeQuery(sqlClient, ACTIVATE_USER, values);
    }

    @Override
    public Future<Optional<User>> getUserByEmail(SqlClient sqlClient, String email) {
        Tuple values = Tuple.of(email);
        return DbUtils.executeQueryAndReturnOne(sqlClient, SELECT_USER_BY_EMAIL, values, User::fromRow);
    }
}
