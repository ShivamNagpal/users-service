package dev.shivamnagpal.users.daos;

import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.sqlclient.SqlClient;

public interface HealthDao {
    Future<Void> pgPoolHealthCheck(SqlClient sqlClient);

    Future<Void> mongoClientHealthCheck(MongoClient mongoClient);
}
