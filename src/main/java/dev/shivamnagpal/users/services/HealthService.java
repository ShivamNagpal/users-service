package dev.shivamnagpal.users.services;

import io.vertx.core.Future;

public interface HealthService {
    Future<String> checkDbHealth();
}
