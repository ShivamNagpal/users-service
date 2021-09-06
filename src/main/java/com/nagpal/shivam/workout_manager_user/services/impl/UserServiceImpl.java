package com.nagpal.shivam.workout_manager_user.services.impl;

import com.nagpal.shivam.workout_manager_user.daos.UserDao;
import com.nagpal.shivam.workout_manager_user.enums.AccountStatus;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.services.UserService;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.pgclient.PgPool;

import java.time.OffsetDateTime;

public class UserServiceImpl implements UserService {
    private final PgPool pgPool;
    private final MongoClient mongoClient;
    private final UserDao userDao;

    public UserServiceImpl(PgPool pgPool, MongoClient mongoClient, UserDao userDao) {
        this.pgPool = pgPool;
        this.mongoClient = mongoClient;
        this.userDao = userDao;
    }

    @Override
    public Future<Void> signUp(User user) {
        user.setDeleted(false);
        OffsetDateTime currentTime = OffsetDateTime.now();
        user.setTimeCreated(currentTime);
        user.setTimeLastModified(currentTime);
        user.setEmailVerified(false);
        user.setAccountStatus(AccountStatus.UNVERIFIED);


        return userDao.signUp(pgPool, user)
                .compose(object -> Future.succeededFuture());
    }
}
