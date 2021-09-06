package com.nagpal.shivam.workout_manager_user.daos.impl;

import com.nagpal.shivam.workout_manager_user.daos.HealthDao;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.DbUtils;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

public class HealthDaoImpl implements HealthDao {

    public HealthDaoImpl() {
    }

    @Override
    public Future<Void> sqlClientHealthCheck(SqlClient sqlClient) {
        return DbUtils.executeQuery(sqlClient, Constants.SELECT_1, Tuple.tuple());
    }

    @Override
    public Future<Void> mongoClientHealthCheck(MongoClient mongoClient) {
        return mongoClient.runCommand(Constants.PING, new JsonObject().put(Constants.PING, 1))
                .compose(ob -> Future.succeededFuture());
    }
}
