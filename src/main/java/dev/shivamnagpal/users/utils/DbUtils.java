package dev.shivamnagpal.users.utils;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.sqlclient.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DbUtils {
    private DbUtils() {
    }

    public static Future<Void> executeQuery(SqlClient sqlClient, String query, Tuple tuple) {
        return sqlClient.preparedQuery(query)
                .execute(tuple)
                .compose(rs -> Future.succeededFuture());
    }

    public static <R> Future<Optional<R>> executeQueryAndReturnOne(SqlClient sqlClient, String query, Tuple tuple,
                                                                   Function<Row, R> mapper) {
        return sqlClient.preparedQuery(query)
                .execute(tuple)
                .map(rs -> {
                    RowIterator<Row> iterator = rs.iterator();
                    if (iterator.hasNext()) {
                        return Optional.of(mapper.apply(iterator.next()));
                    } else {
                        return Optional.empty();
                    }
                });
    }

    public static <R> Future<List<R>> executeQueryAndReturnMany(SqlClient sqlClient, String query, Tuple tuple,
                                                                Function<RowSet<Row>, List<R>> mapper) {
        return sqlClient.preparedQuery(query)
                .execute(tuple)
                .map(mapper);
    }

    public static Future<Void> executeBatch(SqlClient sqlClient, String query, List<Tuple> tuples) {
        return sqlClient.preparedQuery(query)
                .executeBatch(tuples)
                .compose(rowSet -> Future.succeededFuture());
    }

    public static Future<Void> sqlClientHealthCheck(SqlClient sqlClient) {
        return executeQuery(sqlClient, Constants.SELECT_1, Tuple.tuple());
    }

    public static Future<Void> mongoClientHealthCheck(MongoClient mongoClient) {
        return mongoClient.runCommand(Constants.PING, new JsonObject().put(Constants.PING, 1))
                .compose(ob -> Future.succeededFuture());
    }

    public static Long mapRowToId(Row row) {
        return row.getLong(ModelConstants.ID);
    }
}
