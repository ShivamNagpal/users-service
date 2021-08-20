package com.nagpal.shivam.workout_manager_user.utils;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

public class DbUtils {
    private DbUtils() {
    }

    public static Future<Void> executeQuery(SqlClient sqlClient, String query, Tuple tuple) {
        return sqlClient.preparedQuery(query)
                .execute(tuple)
                .compose(rs -> Future.succeededFuture());
    }
}
