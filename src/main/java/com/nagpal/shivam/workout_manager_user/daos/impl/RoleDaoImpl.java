package com.nagpal.shivam.workout_manager_user.daos.impl;

import com.nagpal.shivam.workout_manager_user.daos.RoleDao;
import com.nagpal.shivam.workout_manager_user.enums.RoleName;
import com.nagpal.shivam.workout_manager_user.utils.DbUtils;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.time.OffsetDateTime;
import java.util.Optional;

public class RoleDaoImpl implements RoleDao {
    public static final String INSERT_USER_ROLE = "INSERT INTO \"role\"(user_id, \"role\", deleted, time_created, " +
            "time_last_modified) VALUES($1, $2, $3, $4, $5) RETURNING id";

    @Override
    public Future<Long> insertUserRole(SqlClient sqlClient, Long userId) {
        OffsetDateTime currentTime = OffsetDateTime.now();
        Tuple values = Tuple.of(
                userId,
                RoleName.USER,
                false,
                currentTime,
                currentTime
        );
        return DbUtils.executeQueryAndReturnOne(sqlClient, INSERT_USER_ROLE, values, DbUtils::mapRowToId)
                .map(Optional::get);
    }
}
