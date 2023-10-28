package dev.shivamnagpal.users.controllers;

import dev.shivamnagpal.users.dtos.internal.JWTAuthTokenDTO;
import dev.shivamnagpal.users.dtos.internal.UserUpdateRequestDTO;
import dev.shivamnagpal.users.dtos.request.EmailRequestDTO;
import dev.shivamnagpal.users.dtos.request.LoginRequestDTO;
import dev.shivamnagpal.users.dtos.request.PasswordUpdateRequestDTO;
import dev.shivamnagpal.users.dtos.response.ResponseWrapper;
import dev.shivamnagpal.users.exceptions.handlers.GlobalExceptionHandler;
import dev.shivamnagpal.users.models.User;
import dev.shivamnagpal.users.services.JWTService;
import dev.shivamnagpal.users.services.UserService;
import dev.shivamnagpal.users.utils.AuthenticationUtils;
import dev.shivamnagpal.users.utils.RequestConstants;
import dev.shivamnagpal.users.utils.RequestValidationUtils;
import dev.shivamnagpal.users.utils.RoutingConstants;
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
        resetPassword();
        deactivate();
        reactivate();
        scheduleForDeletion();
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
                        .onSuccess(obj -> {
                                    Object responseWrapper;
                                    if (obj instanceof ResponseWrapper) {
                                        responseWrapper = obj;
                                    } else {
                                        responseWrapper = ResponseWrapper.success(obj);
                                    }
                                    routingContext.response()
                                            .setStatusCode(HttpResponseStatus.OK.code())
                                            .end(Json.encodePrettily(responseWrapper));
                                }
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
                        .compose(EmailRequestDTO::fromRequest)
                        .compose(emailRequestDTO -> {
                            String authToken = AuthenticationUtils.getAuthToken(routingContext.request());
                            JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                            return userService.updateEmail(jwtAuthTokenDTO, emailRequestDTO);
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

    private void resetPassword() {
        router.post(RoutingConstants.RESET_PASSWORD)
                .handler(routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                        .compose(EmailRequestDTO::fromRequest)
                        .compose(userService::resetPassword)
                        .onSuccess(otpResponseDTO -> routingContext.response()
                                .setStatusCode(HttpResponseStatus.OK.code())
                                .end(Json.encodePrettily(ResponseWrapper.success(otpResponseDTO)))
                        )
                        .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                        )
                );
    }

    private void deactivate() {
        router.post(RoutingConstants.DEACTIVATE)
                .handler(routingContext -> {
                    String authToken = AuthenticationUtils.getAuthToken(routingContext.request());
                    JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                    userService.deactivate(jwtAuthTokenDTO)
                            .onSuccess(obj -> routingContext.response()
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                            )
                            .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                            );
                });
    }

    private void reactivate() {
        router.post(RoutingConstants.REACTIVATE)
                .handler(routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                        .compose(LoginRequestDTO::fromRequest)
                        .compose(userService::reactivate)
                        .onSuccess(obj -> routingContext.response()
                                .setStatusCode(HttpResponseStatus.OK.code())
                                .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                        )
                        .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response()))
                );
    }

    private void scheduleForDeletion() {
        router.delete(RoutingConstants.ME)
                .handler(routingContext -> {
                    String authToken = AuthenticationUtils.getAuthToken(routingContext.request());
                    JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                    userService.scheduleForDeletion(jwtAuthTokenDTO)
                            .onSuccess(obj -> routingContext.response()
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                            )
                            .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                            );
                });
    }
}
