package com.nagpal.shivam.workout_manager_user.services;

import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.DbEventAddress;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class HealthService {
    private final Vertx vertx;

    public HealthService(Vertx vertx) {
        this.vertx = vertx;
    }

    public Future<String> checkDbHealth() {
        return vertx.eventBus().request(DbEventAddress.DB_PG_HEALTH, null)
                .map(message -> {
                    boolean result = (Boolean) message.body();
                    return result ? Constants.UP : Constants.DOWN;
                });
    }
}
