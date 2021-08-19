package com.nagpal.shivam.workout_manager_user.configurations;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

public class DatabaseConfiguration {
    private static volatile SqlClient sqlClient;

    public static SqlClient getInstance(Vertx vertx, JsonObject config) {
        if (sqlClient == null) {
            synchronized (DatabaseConfiguration.class) {
                if (sqlClient == null) {
                    PgConnectOptions pgConnectOptions = new PgConnectOptions()
                            .setHost(config.getString("pg.host"))
                            .setPort(config.getInteger("pg.port"))
                            .setDatabase(config.getString("pg.database"))
                            .setUser(config.getString("pg.username"))
                            .setPassword(config.getString("pg.password"));
                    PoolOptions poolOptions = new PoolOptions();
                    sqlClient = PgPool.client(vertx, pgConnectOptions, poolOptions);
                }
            }
        }
        return sqlClient;
    }
}
