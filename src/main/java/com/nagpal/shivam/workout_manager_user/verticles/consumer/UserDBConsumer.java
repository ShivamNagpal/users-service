package com.nagpal.shivam.workout_manager_user.verticles.consumer;

import com.nagpal.shivam.workout_manager_user.daos.UserDao;
import com.nagpal.shivam.workout_manager_user.dtos.ReplyExceptionMessage;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.DbEventAddress;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;

public class UserDBConsumer {
    private final EventBus eventBus;
    private final UserDao userDao;

    public UserDBConsumer(EventBus eventBus, UserDao userDao) {
        this.eventBus = eventBus;
        this.userDao = userDao;

        setupConsumers();
    }

    private void setupConsumers() {
        signUp();
    }

    private void signUp() {
        eventBus.consumer(DbEventAddress.USER_SIGN_UP, message -> {
            User user = (User) message.body();
            userDao.signUp(user)
                    .onSuccess(message::reply)
                    .onFailure(throwable -> {
                        if (throwable instanceof ResponseException) {
                            ResponseException responseException = (ResponseException) throwable;
                            ReplyExceptionMessage replyExceptionMessage =
                                    new ReplyExceptionMessage(responseException.getStatus(),
                                            responseException.getMessage(),
                                            responseException.getPayload());
                            message.fail(Constants.MESSAGE_FAILURE_HANDLED, Json.encode(replyExceptionMessage));
                        } else {
                            message.fail(Constants.MESSAGE_FAILURE_UNHANDLED, throwable.getMessage());
                        }
                    });
        });
    }
}
