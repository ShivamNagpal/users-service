package dev.shivamnagpal.users.services.impl;

import dev.shivamnagpal.users.daos.RoleDao;
import dev.shivamnagpal.users.dtos.response.wrapper.ErrorResponse;
import dev.shivamnagpal.users.enums.ErrorCode;
import dev.shivamnagpal.users.enums.RoleName;
import dev.shivamnagpal.users.exceptions.RestException;
import dev.shivamnagpal.users.helpers.UserHelper;
import dev.shivamnagpal.users.models.Role;
import dev.shivamnagpal.users.services.RoleService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;

public class RoleServiceImpl implements RoleService {
    private final Pool pgPool;

    private final RoleDao roleDao;

    private final UserHelper userHelper;

    public RoleServiceImpl(Pool pgPool, RoleDao roleDao, UserHelper userHelper) {
        this.pgPool = pgPool;
        this.roleDao = roleDao;
        this.userHelper = userHelper;
    }

    @Override
    public Future<Void> assignManagerRole(Long userId) {
        return pgPool.withTransaction(
                sqlConnection -> userHelper.getUserById(sqlConnection, userId)
                        .compose(user -> roleDao.fetchRoleByUserIdAndRoleName(sqlConnection, userId, RoleName.MANAGER))
                        .compose(roleOptional -> {
                            if (roleOptional.isEmpty()) {
                                return roleDao.insertRole(sqlConnection, userId, RoleName.MANAGER)
                                        .compose(id -> Future.succeededFuture());
                            }
                            Role role = roleOptional.get();
                            if (role.getDeleted() != null && !role.getDeleted()) {
                                return Future.failedFuture(
                                        new RestException(
                                                HttpResponseStatus.BAD_REQUEST,
                                                ErrorResponse.from(ErrorCode.USER_IS_ALREADY_A_MANAGER)
                                        )
                                );
                            }
                            return roleDao.updateRoleDeletedStatus(sqlConnection, role.getId(), false);
                        })
        );
    }

    @Override
    public Future<Void> unAssignManagerRole(Long userId) {
        return pgPool.withTransaction(
                sqlConnection -> userHelper.getUserById(sqlConnection, userId)
                        .compose(user -> roleDao.fetchRoleByUserIdAndRoleName(sqlConnection, userId, RoleName.MANAGER))
                        .compose(roleOptional -> {
                            if (roleOptional.isEmpty()) {
                                return Future.failedFuture(
                                        new RestException(
                                                HttpResponseStatus.BAD_REQUEST,
                                                ErrorResponse.from(ErrorCode.USER_IS_NOT_A_MANAGER)
                                        )
                                );
                            }
                            Role role = roleOptional.get();
                            if (role.getDeleted() != null && role.getDeleted()) {
                                return Future.failedFuture(
                                        new RestException(
                                                HttpResponseStatus.BAD_REQUEST,
                                                ErrorResponse.from(ErrorCode.USER_IS_NOT_A_MANAGER)
                                        )
                                );
                            }
                            return roleDao.updateRoleDeletedStatus(sqlConnection, role.getId(), true);
                        })
        );
    }
}
