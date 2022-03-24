package com.nagpal.shivam.workout_manager_user.services;

import io.vertx.core.Future;

public interface HealthService {
    Future<String> checkDbHealth();
}
