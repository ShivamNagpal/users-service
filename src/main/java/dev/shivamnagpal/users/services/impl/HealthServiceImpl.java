package dev.shivamnagpal.users.services.impl;

import dev.shivamnagpal.users.daos.HealthDao;
import dev.shivamnagpal.users.services.HealthService;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.MessageConstants;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.pgclient.PgPool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HealthServiceImpl implements HealthService {

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
                    log.error(MessageConstants.PG_POOL_HEALTH_CHECK_FAILED, throwable);
                    return Future.failedFuture(throwable);
                });
        Future<Void> mongoClientHealthCheckFuture = healthDao.mongoClientHealthCheck(mongoClient)
                .recover(throwable -> {
                    log.error(MessageConstants.MONGO_CLIENT_HEALTH_CHECK_FAILED, throwable);
                    return Future.failedFuture(throwable);
                });
        return Future.join(sqlClientHealthFuture, mongoClientHealthCheckFuture)
                .map(Constants.UP)
                .recover(throwable -> Future.succeededFuture(Constants.DOWN));
    }
}
