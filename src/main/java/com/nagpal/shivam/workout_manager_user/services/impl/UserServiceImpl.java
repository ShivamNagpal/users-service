package com.nagpal.shivam.workout_manager_user.services.impl;

import com.nagpal.shivam.workout_manager_user.daos.UserDao;
import com.nagpal.shivam.workout_manager_user.enums.AccountStatus;
import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.services.OTPService;
import com.nagpal.shivam.workout_manager_user.services.UserService;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.pgclient.PgPool;

import java.time.OffsetDateTime;

public class UserServiceImpl implements UserService {
    private final PgPool pgPool;
    private final MongoClient mongoClient;
    private final UserDao userDao;
    private final OTPService otpService;

    public UserServiceImpl(PgPool pgPool, MongoClient mongoClient, UserDao userDao, OTPService otpService) {
        this.pgPool = pgPool;
        this.mongoClient = mongoClient;
        this.userDao = userDao;
        this.otpService = otpService;
    }

    @Override
    public Future<Void> signUp(User user) {
        user.setDeleted(false);
        OffsetDateTime currentTime = OffsetDateTime.now();
        user.setTimeCreated(currentTime);
        user.setTimeLastModified(currentTime);
        user.setEmailVerified(false);
        user.setAccountStatus(AccountStatus.UNVERIFIED);

        return pgPool.withTransaction(client -> userDao.signUp(client, user)
                        .compose(userId -> otpService.triggerEmailVerification(client, userId, user.getEmail(),
                                OTPPurpose.VERIFY_USER)
                        )
                )
                .compose(object -> Future.succeededFuture());
    }
}
