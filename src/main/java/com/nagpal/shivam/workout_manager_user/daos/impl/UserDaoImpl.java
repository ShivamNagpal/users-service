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

    public static final String SELECT_USER_BY_ID = "SELECT * FROM \"user\" where id=$1";

    public static final String UPDATE_USER =
            "UPDATE \"user\" SET first_name=$1, last_name=$2, time_last_modified=$3 WHERE id=$4";

    public static final String UPDATE_EMAIL = "UPDATE \"user\" SET email=$1, time_last_modified=$2 WHERE id=$3";

    public static final String UPDATE_PASSWORD = "UPDATE \"user\" SET password=$1, time_last_modified=$2 WHERE id=$3";

    public static final String UPDATE_STATUS =
            "UPDATE \"user\" SET account_status=$1, time_last_modified=$2 WHERE id=$3";

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

    @Override
    public Future<Optional<User>> getById(SqlClient sqlClient, Long id) {
        Tuple values = Tuple.of(id);
        return DbUtils.executeQueryAndReturnOne(sqlClient, SELECT_USER_BY_ID, values, User::fromRow);
    }

    @Override
    public Future<Void> update(SqlClient sqlClient, User user) {
        Tuple values = Tuple.of(
                user.getFirstName(),
                user.getLastName(),
                OffsetDateTime.now(),
                user.getId()
        );
        return DbUtils.executeQuery(sqlClient, UPDATE_USER, values);
    }

    @Override
    public Future<Void> updateEmail(SqlClient sqlClient, Long userId, String email) {
        Tuple values = Tuple.of(
                email,
                OffsetDateTime.now(),
                userId
        );
        return DbUtils.executeQuery(sqlClient, UPDATE_EMAIL, values);
    }

    @Override
    public Future<Void> updatePassword(SqlClient sqlClient, Long userId, String password) {
        Tuple values = Tuple.of(
                password,
                OffsetDateTime.now(),
                userId
        );
        return DbUtils.executeQuery(sqlClient, UPDATE_PASSWORD, values);
    }

    @Override
    public Future<Void> updateStatus(SqlClient sqlClient, Long userId, AccountStatus status) {
        Tuple values = Tuple.of(
                status,
                OffsetDateTime.now(),
                userId
        );
        return DbUtils.executeQuery(sqlClient, UPDATE_STATUS, values);
    }
}
