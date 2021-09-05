package com.nagpal.shivam.workout_manager_user.dao;

import com.nagpal.shivam.workout_manager_user.dtos.ReplyExceptionMessage;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.DbEventAddress;
import com.nagpal.shivam.workout_manager_user.utils.DbUtils;
import com.nagpal.shivam.workout_manager_user.utils.PgExceptionCodes;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

public class UserDao {
    private final EventBus eventBus;
    private final SqlClient sqlClient;
    private final MongoClient mongoClient;

    public UserDao(EventBus eventBus, SqlClient sqlClient, MongoClient mongoClient) {
        this.eventBus = eventBus;
        this.sqlClient = sqlClient;
        this.mongoClient = mongoClient;

        setupConsumers();
    }

    private void setupConsumers() {
        signUp();
    }

    private void signUp() {
        eventBus.consumer(DbEventAddress.USER_SIGN_UP, message -> {
            User user = (User) message.body();
            String sql = "INSERT INTO \"user\"" +
                    "(first_name, last_name, email, \"password\", email_verified, account_status, meta, deleted, " +
                    "time_created, time_last_modified) " +
                    "VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10) RETURNING id";
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
            DbUtils.executeQueryAndReturnOne(sqlClient, sql, values, row -> row.getLong("id"))
                    .onSuccess(message::reply)
                    .onFailure(throwable -> {
                        if (throwable instanceof PgException) {
                            PgException pgException = (PgException) throwable;
                            if (pgException.getCode().equals(PgExceptionCodes.UNIQUE_KEY_CONSTRAINT_VIOLATION)) {
                                ReplyExceptionMessage replyExceptionMessage =
                                        new ReplyExceptionMessage(HttpResponseStatus.BAD_REQUEST.code(),
                                                "User with " + pgException.getDetail(), null);
                                message.fail(Constants.MESSAGE_FAILURE_HANDLED, Json.encode(
                                        replyExceptionMessage));
                            } else {
                                message.fail(Constants.MESSAGE_FAILURE_UNHANDLED, pgException.getMessage());
                            }
                        } else {
                            message.fail(Constants.MESSAGE_FAILURE_UNHANDLED, throwable.getMessage());
                        }
                    });
        });
    }
}
