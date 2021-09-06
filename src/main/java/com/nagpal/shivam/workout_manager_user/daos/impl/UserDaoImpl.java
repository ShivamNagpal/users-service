package com.nagpal.shivam.workout_manager_user.daos.impl;

import com.nagpal.shivam.workout_manager_user.daos.UserDao;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.utils.DbUtils;
import com.nagpal.shivam.workout_manager_user.utils.ModelConstants;
import com.nagpal.shivam.workout_manager_user.utils.PgExceptionCodes;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.util.Optional;

public class UserDaoImpl implements UserDao {
    public static final String INSERT_USER = "INSERT INTO \"user\"" +
            "(first_name, last_name, email, \"password\", email_verified, account_status, meta, deleted, " +
            "time_created, time_last_modified) " +
            "VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10) RETURNING id";

    public UserDaoImpl() {
    }

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
        return DbUtils.executeQueryAndReturnOne(sqlClient, INSERT_USER, values, row -> row.getLong(ModelConstants.ID))
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
}
