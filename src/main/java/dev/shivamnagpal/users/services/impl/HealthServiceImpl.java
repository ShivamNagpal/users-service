package dev.shivamnagpal.users.services.impl;

import dev.shivamnagpal.users.daos.HealthDao;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.MessageConstants;
import dev.shivamnagpal.users.services.HealthService;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.pgclient.PgPool;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HealthServiceImpl implements HealthService {
    private static final Logger logger = Logger.getLogger(HealthServiceImpl.class.getName());
    private final PgPool pgPool;
    private final MongoClient mongoClient;
    private final HealthDao healthDao;

    public HealthServiceImpl(PgPool pgPool, MongoClient mongoClient, HealthDao healthDao) {
        this.pgPool = pgPool;
        this.mongoClient = mongoClient;
        this.healthDao = healthDao;
    }

    @Override
    public Future<String> checkDbHealth() {
        Future<Void> sqlClientHealthFuture = healthDao.pgPoolHealthCheck(pgPool)
                .recover(throwable -> {
                    logger.log(Level.SEVERE, MessageConstants.PG_POOL_HEALTH_CHECK_FAILED, throwable);
                    return Future.failedFuture(throwable);
                });
        Future<Void> mongoClientHealthCheckFuture = healthDao.mongoClientHealthCheck(mongoClient)
                .recover(throwable -> {
                    logger.log(Level.SEVERE, MessageConstants.MONGO_CLIENT_HEALTH_CHECK_FAILED, throwable);
                    return Future.failedFuture(throwable);
                });
        return CompositeFuture.join(sqlClientHealthFuture, mongoClientHealthCheckFuture)
                .map(Constants.UP)
                .recover(throwable -> Future.succeededFuture(Constants.DOWN));
    }
}
