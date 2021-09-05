package com.nagpal.shivam.workout_manager_user.services.impl;

import com.nagpal.shivam.workout_manager_user.enums.AccountStatus;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.services.UserService;
import com.nagpal.shivam.workout_manager_user.utils.DbEventAddress;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import java.time.OffsetDateTime;

public class UserServiceImpl implements UserService {
    private final Vertx vertx;
    private EventBus eventBus;

    public UserServiceImpl(Vertx vertx) {
        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
    }

    @Override
    public Future<Void> signUp(User user) {
        user.setDeleted(false);
        OffsetDateTime currentTime = OffsetDateTime.now();
        user.setTimeCreated(currentTime);
        user.setTimeLastModified(currentTime);
        user.setEmailVerified(false);
        user.setAccountStatus(AccountStatus.UNVERIFIED);

        return eventBus.request(DbEventAddress.USER_SIGN_UP, user)
                .compose(object -> Future.succeededFuture());
    }
}
