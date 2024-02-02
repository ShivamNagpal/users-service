package dev.shivamnagpal.users.controllers;

import dev.shivamnagpal.users.core.Controller;
import dev.shivamnagpal.users.core.RequestPath;
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
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class UserController extends Controller {

    private final JsonObject config;

    private final JWTService jwtService;

    private final UserService userService;

    public UserController(
            Router router,
            RequestPath requestPath,
            JsonObject config,
            UserService userService,
            JWTService jwtService
    ) {
        super(router, requestPath.next(RoutingConstants.USER));

        this.config = config;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public void registerRoutes() {
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
        super.router.post(super.requestPath.next(RoutingConstants.SIGN_UP).path())
                .handler(
                        routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                                .compose(body -> User.fromRequest(body, config))
                                .compose(userService::signUp)
                                .onSuccess(
                                        otpResponseDTO -> routingContext.response()
                                                .setStatusCode(HttpResponseStatus.CREATED.code())
                                                .end(Json.encodePrettily(ResponseWrapper.success(otpResponseDTO)))
                                )
                                .onFailure(
                                        throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                                )
                );
    }

    private void login() {
        super.router.post(super.requestPath.next(RoutingConstants.LOGIN).path())
                .handler(
                        routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                                .compose(LoginRequestDTO::fromRequest)
                                .compose(userService::login)
                                .onSuccess(
                                        loginResponseDTO -> routingContext.response()
                                                .setStatusCode(HttpResponseStatus.OK.code())
                                                .end(Json.encodePrettily(ResponseWrapper.success(loginResponseDTO)))
                                )
                                .onFailure(
                                        throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                                )
                );
    }

    private void logout() {
        super.router.post(super.requestPath.next(RoutingConstants.LOGOUT).path())
                .handler(routingContext -> {
                    HttpServerRequest request = routingContext.request();
                    RequestValidationUtils.getBooleanQueryParam(request, RequestConstants.ALL_SESSIONS, false)
                            .compose(allSessions -> {
                                String authorizationValue = AuthenticationUtils.getAuthToken(request);
                                JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authorizationValue);
                                return userService.logout(jwtAuthTokenDTO, allSessions);
                            })
                            .onSuccess(
                                    obj -> routingContext.response()
                                            .setStatusCode(HttpResponseStatus.OK.code())
                                            .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                            )
                            .onFailure(
                                    throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                            );
                });
    }

    private void getById() {
        super.router.get(super.requestPath.next(RoutingConstants.ME).path())
                .handler(routingContext -> {
                    HttpServerRequest request = routingContext.request();
                    String authToken = AuthenticationUtils.getAuthToken(request);
                    JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                    userService.getById(jwtAuthTokenDTO)
                            .onSuccess(
                                    userResponseDTO -> routingContext.response()
                                            .setStatusCode(HttpResponseStatus.OK.code())
                                            .end(Json.encodePrettily(ResponseWrapper.success(userResponseDTO)))
                            )
                            .onFailure(
                                    throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                            );
                });
    }

    private void update() {
        super.router.put(super.requestPath.next(RoutingConstants.ME).path())
                .handler(
                        routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                                .compose(UserUpdateRequestDTO::fromRequest)
                                .compose(userUpdateRequestDTO -> {
                                    HttpServerRequest request = routingContext.request();
                                    String authToken = AuthenticationUtils.getAuthToken(request);
                                    JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                                    return userService.update(jwtAuthTokenDTO, userUpdateRequestDTO);
                                })
                                .onSuccess(
                                        userResponseDTO -> routingContext.response()
                                                .setStatusCode(HttpResponseStatus.OK.code())
                                                .end(Json.encodePrettily(ResponseWrapper.success(userResponseDTO)))
                                )
                                .onFailure(
                                        throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                                )
                );
    }

    private void updateEmail() {
        super.router.patch(super.requestPath.next(RoutingConstants.EMAIL).path())
                .handler(
                        routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                                .compose(EmailRequestDTO::fromRequest)
                                .compose(emailRequestDTO -> {
                                    String authToken = AuthenticationUtils.getAuthToken(routingContext.request());
                                    JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                                    return userService.updateEmail(jwtAuthTokenDTO, emailRequestDTO);
                                })
                                .onSuccess(
                                        otpResponseDTO -> routingContext.response()
                                                .setStatusCode(HttpResponseStatus.OK.code())
                                                .end(Json.encodePrettily(ResponseWrapper.success(otpResponseDTO)))
                                )
                                .onFailure(
                                        throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                                )
                );
    }

    private void updatePassword() {
        super.router.patch(super.requestPath.next(RoutingConstants.PASSWORD).path())
                .handler(
                        routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                                .compose(body -> PasswordUpdateRequestDTO.fromRequest(body, config))
                                .compose(passwordUpdateRequestDTO -> {
                                    String authToken = AuthenticationUtils.getAuthToken(routingContext.request());
                                    JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                                    return userService.updatePassword(jwtAuthTokenDTO, passwordUpdateRequestDTO);
                                })
                                .onSuccess(
                                        loginResponseDTO -> routingContext.response()
                                                .setStatusCode(HttpResponseStatus.OK.code())
                                                .end(Json.encodePrettily(ResponseWrapper.success(loginResponseDTO)))
                                )
                                .onFailure(
                                        throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                                )
                );
    }

    private void resetPassword() {
        super.router.post(super.requestPath.next(RoutingConstants.RESET_PASSWORD).path())
                .handler(
                        routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                                .compose(EmailRequestDTO::fromRequest)
                                .compose(userService::resetPassword)
                                .onSuccess(
                                        otpResponseDTO -> routingContext.response()
                                                .setStatusCode(HttpResponseStatus.OK.code())
                                                .end(Json.encodePrettily(ResponseWrapper.success(otpResponseDTO)))
                                )
                                .onFailure(
                                        throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                                )
                );
    }

    private void deactivate() {
        super.router.post(super.requestPath.next(RoutingConstants.DEACTIVATE).path())
                .handler(routingContext -> {
                    String authToken = AuthenticationUtils.getAuthToken(routingContext.request());
                    JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                    userService.deactivate(jwtAuthTokenDTO)
                            .onSuccess(
                                    obj -> routingContext.response()
                                            .setStatusCode(HttpResponseStatus.OK.code())
                                            .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                            )
                            .onFailure(
                                    throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                            );
                });
    }

    private void reactivate() {
        super.router.post(super.requestPath.next(RoutingConstants.REACTIVATE).path())
                .handler(
                        routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                                .compose(LoginRequestDTO::fromRequest)
                                .compose(userService::reactivate)
                                .onSuccess(
                                        obj -> routingContext.response()
                                                .setStatusCode(HttpResponseStatus.OK.code())
                                                .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                                )
                                .onFailure(
                                        throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                                )
                );
    }

    private void scheduleForDeletion() {
        super.router.delete(super.requestPath.next(RoutingConstants.ME).path())
                .handler(routingContext -> {
                    String authToken = AuthenticationUtils.getAuthToken(routingContext.request());
                    JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                    userService.scheduleForDeletion(jwtAuthTokenDTO)
                            .onSuccess(
                                    obj -> routingContext.response()
                                            .setStatusCode(HttpResponseStatus.OK.code())
                                            .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                            )
                            .onFailure(
                                    throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                            );
                });
    }
}
