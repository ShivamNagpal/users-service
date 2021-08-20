package com.nagpal.shivam.workout_manager_user.configurations;

import com.nagpal.shivam.workout_manager_user.enums.Configuration;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConfiguration {
    private static final Logger logger = Logger.getLogger(DatabaseConfiguration.class.getName());
    private static volatile SqlClient sqlClient;

    private DatabaseConfiguration() {
    }

    public static SqlClient getInstance(Vertx vertx, JsonObject config) {
        if (sqlClient == null) {
            synchronized (DatabaseConfiguration.class) {
                if (sqlClient == null) {
                    PgConnectOptions pgConnectOptions = new PgConnectOptions()
                            .setHost(config.getString(Configuration.PG_HOST.getKey()))
                            .setPort(config.getInteger(Configuration.PG_PORT.getKey()))
                            .setDatabase(config.getString(Configuration.PG_DATABASE.getKey()))
                            .setUser(config.getString(Configuration.PG_USERNAME.getKey()))
                            .setPassword(config.getString(Configuration.PG_PASSWORD.getKey()))
                            .setCachePreparedStatements(true);
                    PoolOptions poolOptions = new PoolOptions();
                    sqlClient = PgPool.client(vertx, pgConnectOptions, poolOptions);
                    logger.log(Level.INFO, MessageConstants.SUCCESSFULLY_CREATED_SQL_CLIENT_INSTANCE);
                }
            }
        }
        return sqlClient;
    }
}
