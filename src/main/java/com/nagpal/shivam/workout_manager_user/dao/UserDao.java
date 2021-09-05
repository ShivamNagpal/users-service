package com.nagpal.shivam.workout_manager_user.dao;

import com.nagpal.shivam.workout_manager_user.dtos.ReplyExceptionMessage;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.utils.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.util.Optional;

public class UserDao {
    public static final String INSERT_USER = "INSERT INTO \"user\"" +
            "(first_name, last_name, email, \"password\", email_verified, account_status, meta, deleted, " +
            "time_created, time_last_modified) " +
            "VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10) RETURNING id";
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
            signUp(user)
                    .onSuccess(message::reply)
                    .onFailure(throwable -> {
                        if (throwable instanceof ResponseException) {
                            ResponseException responseException = (ResponseException) throwable;
                            ReplyExceptionMessage replyExceptionMessage =
                                    new ReplyExceptionMessage(responseException.getStatus(),
                                            responseException.getMessage(),
                                            responseException.getPayload());
                            message.fail(Constants.MESSAGE_FAILURE_HANDLED, Json.encode(
                                    replyExceptionMessage));
                        } else {
                            message.fail(Constants.MESSAGE_FAILURE_UNHANDLED, throwable.getMessage());
                        }
                    });
        });
    }

    private Future<Long> signUp(User user) {
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
