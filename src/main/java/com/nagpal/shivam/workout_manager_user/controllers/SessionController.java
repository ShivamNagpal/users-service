package com.nagpal.shivam.workout_manager_user.controllers;

import com.nagpal.shivam.workout_manager_user.dtos.request.RefreshSessionRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.ResponseWrapper;
import com.nagpal.shivam.workout_manager_user.exceptions.handlers.GlobalExceptionHandler;
import com.nagpal.shivam.workout_manager_user.services.SessionService;
import com.nagpal.shivam.workout_manager_user.utils.RequestValidationUtils;
import com.nagpal.shivam.workout_manager_user.utils.RoutingConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;

public class SessionController {
    private final Router router;
    private final SessionService sessionService;

    public SessionController(Vertx vertx, Router mainRouter, SessionService sessionService) {
        this.router = Router.router(vertx);
        mainRouter.mountSubRouter(RoutingConstants.SESSION, router);
        this.sessionService = sessionService;

        setupEndpoints();
    }

    private void setupEndpoints() {
        refreshToken();
    }

    private void refreshToken() {
        router.post(RoutingConstants.REFRESH_TOKEN)
                .handler(routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                        .compose(RefreshSessionRequestDTO::fromRequest)
                        .compose(sessionService::refreshSession)
                        .onSuccess(obj -> routingContext.response()
                                .setStatusCode(HttpResponseStatus.OK.code())
                                .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                        )
                        .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response()))
                );
    }
}
