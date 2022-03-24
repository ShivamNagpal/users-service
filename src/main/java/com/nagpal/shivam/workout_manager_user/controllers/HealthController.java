package com.nagpal.shivam.workout_manager_user.controllers;

import com.nagpal.shivam.workout_manager_user.exceptions.handlers.GlobalExceptionHandler;
import com.nagpal.shivam.workout_manager_user.services.HealthService;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.RoutingConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class HealthController {
    private final Router router;
    private final HealthService healthService;

    public HealthController(Vertx vertx, Router mainRouter, HealthService healthService) {
        this.router = Router.router(vertx);
        mainRouter.mountSubRouter(RoutingConstants.HEALTH, router);
        this.healthService = healthService;

        setupEndpoints();
    }

    private void setupEndpoints() {
        appHealth();
        dbHealth();
    }

    private void appHealth() {
        router.route(RoutingConstants.PATH_SEPARATOR)
                .handler(routingContext -> routingContext.response().setStatusCode(HttpResponseStatus.OK.code())
                        .end(Constants.UP)
                );
    }

    private void dbHealth() {
        router.route(RoutingConstants.DB)
                .handler(routingContext -> healthService.checkDbHealth()
                        .onSuccess(message -> routingContext.response().setStatusCode(HttpResponseStatus.OK.code())
                                .end(message))
                        .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response()))
                );
    }
}
