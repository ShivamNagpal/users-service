package dev.shivamnagpal.users.services;

import io.vertx.core.Future;

public interface RoleService {
    Future<Void> assignManagerRole(Long userId);

    Future<Void> unAssignManagerRole(Long userId);
}
