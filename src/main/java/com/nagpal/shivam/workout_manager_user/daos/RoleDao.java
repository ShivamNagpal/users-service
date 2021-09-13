package com.nagpal.shivam.workout_manager_user.daos;

import com.nagpal.shivam.workout_manager_user.models.Role;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

import java.util.List;

public interface RoleDao {
    Future<Long> insertUserRole(SqlClient sqlClient, Long userId);

    Future<List<Role>> fetchRolesByUserIdAndDeleted(SqlClient sqlClient, Long userId, boolean deleted);
}
