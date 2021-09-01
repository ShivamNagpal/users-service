package com.nagpal.shivam.workout_manager_user.verticles;

import com.nagpal.shivam.workout_manager_user.configurations.DatabaseConfiguration;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.DbEventAddress;
import com.nagpal.shivam.workout_manager_user.utils.DbUtils;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.sqlclient.SqlClient;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseVerticle extends AbstractVerticle {

    private static final Logger logger = Logger.getLogger(DatabaseVerticle.class.getName());

    public static Future<String> deploy(Vertx vertx, JsonObject config) {
        DeploymentOptions databaseDeploymentOptions = new DeploymentOptions()
                .setInstances(config.getInteger(Constants.AVAILABLE_PROCESSORS))
                .setConfig(config);
        return vertx.deployVerticle(DatabaseVerticle.class.getName(),
                        databaseDeploymentOptions)
                .onSuccess(dbDepId -> logger.log(Level.INFO,
                        MessageConstants.SUCCESSFULLY_CONNECTED_TO_THE_POSTGRESQL_DATABASE));
    }

    @Override
    public void start(Promise<Void> startPromise) {
        String startVerticleMessage =
                MessageFormat.format(MessageConstants.STARTING_VERTICLE, this.getClass().getSimpleName());
        logger.info(startVerticleMessage);
        this.setupDatabases(vertx, this.config())
                .onSuccess(a -> startPromise.complete())
                .onFailure(startPromise::fail);
    }

    private Future<Void> setupDatabases(Vertx vertx, JsonObject config) {
        SqlClient sqlClient = DatabaseConfiguration.getSqlClient(vertx, config);
        MongoClient mongoClient = DatabaseConfiguration.getMongoClient(vertx, config);
        return CompositeFuture.all(DbUtils.sqlClientHealthCheck(sqlClient), DbUtils.mongoClientHealthCheck(mongoClient))
                .compose(compositeFuture -> {
                    setupDAOs(vertx, sqlClient, mongoClient);
                    return Future.succeededFuture();
                });
    }

    private void setupDAOs(Vertx vertx, SqlClient sqlClient, MongoClient mongoClient) {
        vertx.eventBus().consumer(DbEventAddress.DB_PG_HEALTH, event -> DbUtils.sqlClientHealthCheck(sqlClient)
                .onSuccess(v -> event.reply(true))
                .onFailure(throwable -> event.reply(false))
        );

        vertx.eventBus().consumer(DbEventAddress.DB_MONGO_HEALTH, event -> DbUtils.mongoClientHealthCheck(mongoClient)
                .onSuccess(v -> event.reply(true))
                .onFailure(throwable -> event.reply(false))
        );
    }
}
