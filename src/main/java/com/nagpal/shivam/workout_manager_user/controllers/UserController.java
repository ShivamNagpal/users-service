package com.nagpal.shivam.workout_manager_user.controllers;

import com.nagpal.shivam.workout_manager_user.dtos.response.ResponseWrapper;
import com.nagpal.shivam.workout_manager_user.exceptions.handlers.GlobalExceptionHandler;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.services.UserService;
import com.nagpal.shivam.workout_manager_user.utils.RequestValidationUtils;
import com.nagpal.shivam.workout_manager_user.utils.RoutingConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class UserController {
    private final Router router;
    private final JsonObject config;
    private final UserService userService;

    public UserController(Vertx vertx, JsonObject config, Router mainRouter, UserService userService) {
        this.router = Router.router(vertx);
        this.config = config;
        mainRouter.mountSubRouter(RoutingConstants.USER, router);
        this.userService = userService;

        setupEndpoints();
    }

    private void setupEndpoints() {
        signUp();
    }

    private void signUp() {
        router.post(RoutingConstants.SIGN_UP)
                .handler(routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                        .compose(body -> User.fromRequest(body, config))
                        .compose(userService::signUp)
                        .onSuccess(otpResponseDTO -> routingContext.response()
                                .setStatusCode(HttpResponseStatus.CREATED.code())
                                .end(Json.encodePrettily(ResponseWrapper.success(otpResponseDTO)))
                        )
                        .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response()))
                );
    }
}
