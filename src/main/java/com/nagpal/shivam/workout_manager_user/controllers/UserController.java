package com.nagpal.shivam.workout_manager_user.controllers;

import com.nagpal.shivam.workout_manager_user.dtos.request.LoginRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.ResponseWrapper;
import com.nagpal.shivam.workout_manager_user.exceptions.handlers.GlobalExceptionHandler;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.services.JWTService;
import com.nagpal.shivam.workout_manager_user.services.UserService;
import com.nagpal.shivam.workout_manager_user.utils.AuthenticationUtils;
import com.nagpal.shivam.workout_manager_user.utils.RequestConstants;
import com.nagpal.shivam.workout_manager_user.utils.RequestValidationUtils;
import com.nagpal.shivam.workout_manager_user.utils.RoutingConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class UserController {
    private final Router router;
    private final JsonObject config;
    private final JWTService jwtService;
    private final UserService userService;

    public UserController(Vertx vertx, JsonObject config, Router mainRouter, UserService userService,
                          JWTService jwtService) {
        this.router = Router.router(vertx);
        this.config = config;
        this.userService = userService;
        this.jwtService = jwtService;
        mainRouter.mountSubRouter(RoutingConstants.USER, router);

        setupEndpoints();
    }

    private void setupEndpoints() {
        signUp();
        login();
        logout();
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

    private void login() {
        router.post(RoutingConstants.LOGIN)
                .handler(routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                        .compose(LoginRequestDTO::fromRequest)
                        .compose(userService::login)
                        .onSuccess(obj -> routingContext.response()
                                .setStatusCode(HttpResponseStatus.OK.code())
                                .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                        )
                        .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response()))
                );
    }

    private void logout() {
        router.post(RoutingConstants.LOGOUT)
                .handler(routingContext -> {
                    HttpServerRequest request = routingContext.request();
                    RequestValidationUtils.getBooleanQueryParam(request, RequestConstants.ALL_SESSIONS, false)
                            .compose(allSessions -> {
                                String authorizationValue = AuthenticationUtils.getAuthToken(request);
                                return userService.logout(jwtService.decodeAuthToken(authorizationValue), allSessions);
                            })
                            .onSuccess(obj -> routingContext.response()
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                            )
                            .onFailure(
                                    throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response()));
                });
    }
}
