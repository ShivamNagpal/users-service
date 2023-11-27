package dev.shivamnagpal.users.daos.impl;

import dev.shivamnagpal.users.daos.HealthDao;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.DbUtils;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

public class HealthDaoImpl implements HealthDao {

    public HealthDaoImpl() {
        // No dependency required for the HealthDaoImpl
    }

    @Override
    public Future<Void> pgPoolHealthCheck(SqlClient sqlClient) {
        return DbUtils.executeQuery(sqlClient, Constants.SELECT_1, Tuple.tuple());
    }

    @Override
    public Future<Void> mongoClientHealthCheck(MongoClient mongoClient) {
        return mongoClient.runCommand(Constants.PING, new JsonObject().put(Constants.PING, 1))
                .compose(ob -> Future.succeededFuture());
    }
}
