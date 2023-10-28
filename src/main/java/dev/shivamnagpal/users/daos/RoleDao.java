package dev.shivamnagpal.users.daos;

import dev.shivamnagpal.users.enums.RoleName;
import dev.shivamnagpal.users.models.Role;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

import java.util.List;
import java.util.Optional;

public interface RoleDao {
    Future<Long> insertRole(SqlClient sqlClient, Long userId, RoleName roleName);

    Future<List<Role>> fetchRolesByUserIdAndDeleted(SqlClient sqlClient, Long userId, boolean deleted);

    Future<Optional<Role>> fetchRoleByUserIdAndRoleName(SqlClient sqlClient, Long userId, RoleName roleName);

    Future<Void> updateRoleDeletedStatus(SqlClient sqlClient, Long id, boolean deleted);
}
