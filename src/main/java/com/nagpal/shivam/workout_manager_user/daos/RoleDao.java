package com.nagpal.shivam.workout_manager_user.daos;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

public interface RoleDao {
    Future<Long> insertUserRole(SqlClient sqlClient, Long userId);
}
