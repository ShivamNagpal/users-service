package com.nagpal.shivam.workout_manager_user.controllers;

import com.nagpal.shivam.workout_manager_user.dtos.internal.JWTAuthTokenDTO;
import com.nagpal.shivam.workout_manager_user.dtos.internal.UserUpdateRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.EmailUpdateRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.LoginRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.PasswordUpdateRequestDTO;
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
        getById();
        update();
        updateEmail();
        updatePassword();
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
                                JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authorizationValue);
                                return userService.logout(jwtAuthTokenDTO, allSessions);
                            })
                            .onSuccess(obj -> routingContext.response()
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                            )
                            .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                            );
                });
    }

    private void getById() {
        router.get(RoutingConstants.ME)
                .handler(routingContext -> {
                    HttpServerRequest request = routingContext.request();
                    String authToken = AuthenticationUtils.getAuthToken(request);
                    JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                    userService.getById(jwtAuthTokenDTO)
                            .onSuccess(userResponseDTO -> routingContext.response()
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(ResponseWrapper.success(userResponseDTO)))
                            )
                            .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                            );
                });
    }

    private void update() {
        router.put(RoutingConstants.ME)
                .handler(routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                        .compose(UserUpdateRequestDTO::fromRequest)
                        .compose(userUpdateRequestDTO -> {
                            HttpServerRequest request = routingContext.request();
                            String authToken = AuthenticationUtils.getAuthToken(request);
                            JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                            return userService.update(jwtAuthTokenDTO, userUpdateRequestDTO);
                        })
                        .onSuccess(userResponseDTO -> routingContext.response()
                                .setStatusCode(HttpResponseStatus.OK.code())
                                .end(Json.encodePrettily(ResponseWrapper.success(userResponseDTO)))
                        )
                        .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                        ));
    }

    private void updateEmail() {
        router.patch(RoutingConstants.EMAIL)
                .handler(routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                        .compose(EmailUpdateRequestDTO::fromRequest)
                        .compose(emailUpdateRequestDTO -> {
                            String authToken = AuthenticationUtils.getAuthToken(routingContext.request());
                            JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                            return userService.updateEmail(jwtAuthTokenDTO, emailUpdateRequestDTO);
                        })
                        .onSuccess(otpResponseDTO -> routingContext.response()
                                .setStatusCode(HttpResponseStatus.OK.code())
                                .end(Json.encodePrettily(ResponseWrapper.success(otpResponseDTO)))
                        )
                        .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                        )
                );
    }

    private void updatePassword() {
        router.patch(RoutingConstants.PASSWORD)
                .handler(routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                        .compose(body -> PasswordUpdateRequestDTO.fromRequest(body, config))
                        .compose(passwordUpdateRequestDTO -> {
                            String authToken = AuthenticationUtils.getAuthToken(routingContext.request());
                            JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                            return userService.updatePassword(jwtAuthTokenDTO, passwordUpdateRequestDTO);
                        })
                        .onSuccess(loginResponseDTO -> routingContext.response()
                                .setStatusCode(HttpResponseStatus.OK.code())
                                .end(Json.encodePrettily(ResponseWrapper.success(loginResponseDTO)))
                        )
                        .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response()))
                );
    }
}
