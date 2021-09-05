package com.nagpal.shivam.workout_manager_user.services.impl;

import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.DbEventAddress;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class HealthServiceImpl implements com.nagpal.shivam.workout_manager_user.services.HealthService {
    private final Vertx vertx;

    public HealthServiceImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<String> checkDbHealth() {
        return vertx.eventBus().request(DbEventAddress.DB_PG_HEALTH, null)
                .map(message -> {
                    boolean result = (Boolean) message.body();
                    return result ? Constants.UP : Constants.DOWN;
                });
    }
}
