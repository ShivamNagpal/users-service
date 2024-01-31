package dev.shivamnagpal.users.configurations;

import dev.shivamnagpal.users.enums.Configuration;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.MessageConstants;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;

import java.text.MessageFormat;

@Slf4j
public class DatabaseConfiguration {

    private static volatile Pool pgPool;

    private DatabaseConfiguration() {
    }

    public static Pool getPostgresPool(Vertx vertx, JsonObject config) {
        if (pgPool == null) {
            synchronized (DatabaseConfiguration.class) {
                if (pgPool == null) {
                    PgConnectOptions pgConnectOptions = new PgConnectOptions()
                            .setHost(config.getString(Configuration.PG_HOST.getKey()))
                            .setPort(config.getInteger(Configuration.PG_PORT.getKey()))
                            .setDatabase(config.getString(Configuration.PG_DATABASE.getKey()))
                            .setUser(config.getString(Configuration.PG_USERNAME.getKey()))
                            .setPassword(config.getString(Configuration.PG_PASSWORD.getKey()))
                            .setCachePreparedStatements(true);
                    PoolOptions poolOptions = new PoolOptions()
                            .setShared(true)
                            .setName(Constants.PG_SHARED_POOL);
                    pgPool = PgBuilder
                            .pool()
                            .connectingTo(pgConnectOptions)
                            .with(poolOptions)
                            .using(vertx)
                            .build();
                    log.info(MessageConstants.SUCCESSFULLY_CREATED_SQL_CLIENT_INSTANCE);
                }
            }
        }
        return pgPool;
    }

    public static MongoClient getMongoClient(Vertx vertx, JsonObject config) {
        return MongoClient.createShared(
                vertx,
                new JsonObject()
                        .put(Constants.CONNECTION_STRING, config.getString(Configuration.MONGO_CONNECTION_URI.getKey()))
                        .put(Constants.DB_NAME, config.getString(Configuration.MONGO_DATABASE.getKey()))
        );
    }

    public static void initFlyway(JsonObject config) {
        String jdbcUri = MessageFormat.format(
                Constants.JDBC_POSTGRESQL_URI,
                config.getString(Configuration.PG_HOST.getKey()),
                config.getString(Configuration.PG_PORT.getKey()),
                config.getString(Configuration.PG_DATABASE.getKey())
        );
        Flyway flyway = Flyway.configure()
                .dataSource(
                        jdbcUri,
                        config.getString(Configuration.PG_USERNAME.getKey()),
                        config.getString(Configuration.PG_PASSWORD.getKey())
                )
                .load();
        flyway.migrate();
    }

    public static Future<Void> verifyMongoIndices(Vertx vertx, JsonObject config) {
        MongoClient mongoClient = getMongoClient(vertx, config);
        return mongoClient.createIndex(Constants.SESSION, new JsonObject().put(Constants.USER_ID, 1));
    }
}
