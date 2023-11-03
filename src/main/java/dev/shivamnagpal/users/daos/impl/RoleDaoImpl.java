package dev.shivamnagpal.users.daos.impl;

import dev.shivamnagpal.users.daos.RoleDao;
import dev.shivamnagpal.users.enums.RoleName;
import dev.shivamnagpal.users.models.Role;
import dev.shivamnagpal.users.utils.DbUtils;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public class RoleDaoImpl implements RoleDao {
    public static final String INSERT_USER_ROLE = "INSERT INTO \"role\"(user_id, \"role\", deleted, time_created, " +
            "time_last_modified) VALUES($1, $2, $3, $4, $5) RETURNING id";

    public static final String SELECT_ROLES_BY_USER_ID_AND_DELETED = "SELECT * FROM \"role\" WHERE user_id=$1 and " +
            "deleted=$2";

    public static final String SELECT_ROLES_BY_USER_ID_AND_ROLE_NAME = "SELECT * FROM \"role\" WHERE user_id=$1 and " +
            "role=$2";

    public static final String UPDATE_ROLE_DELETED_STATUS = "UPDATE \"role\" SET deleted=$1, time_last_modified=$2 " +
            "WHERE id=$3";

    @Override
    public Future<Long> insertRole(SqlClient sqlClient, Long userId, RoleName roleName) {
        OffsetDateTime currentTime = OffsetDateTime.now();
        Tuple values = Tuple.of(
                userId,
                roleName,
                false,
                currentTime,
                currentTime
        );
        return DbUtils.executeQueryAndReturnOne(sqlClient, INSERT_USER_ROLE, values, DbUtils::mapRowToId)
                .map(Optional::get);
    }

    @Override
    public Future<List<Role>> fetchRolesByUserIdAndDeleted(SqlClient sqlClient, Long userId, boolean deleted) {
        Tuple values = Tuple.of(userId, deleted);
        return DbUtils.executeQueryAndReturnMany(
                sqlClient,
                SELECT_ROLES_BY_USER_ID_AND_DELETED,
                values,
                Role::fromRows
        );
    }

    @Override
    public Future<Optional<Role>> fetchRoleByUserIdAndRoleName(SqlClient sqlClient, Long userId, RoleName roleName) {
        Tuple values = Tuple.of(userId, roleName);
        return DbUtils.executeQueryAndReturnOne(
                sqlClient,
                SELECT_ROLES_BY_USER_ID_AND_ROLE_NAME,
                values,
                Role::fromRow
        );
    }

    @Override
    public Future<Void> updateRoleDeletedStatus(SqlClient sqlClient, Long id, boolean deleted) {
        Tuple values = Tuple.of(deleted, OffsetDateTime.now(), id);
        return DbUtils.executeQuery(sqlClient, UPDATE_ROLE_DELETED_STATUS, values);
    }
}
