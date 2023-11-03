package dev.shivamnagpal.users.controllers;

import dev.shivamnagpal.users.dtos.internal.JWTAuthTokenDTO;
import dev.shivamnagpal.users.dtos.request.RoleUpdateRequestDTO;
import dev.shivamnagpal.users.dtos.response.ResponseWrapper;
import dev.shivamnagpal.users.enums.RoleName;
import dev.shivamnagpal.users.exceptions.handlers.GlobalExceptionHandler;
import dev.shivamnagpal.users.services.JWTService;
import dev.shivamnagpal.users.services.RoleService;
import dev.shivamnagpal.users.utils.AuthenticationUtils;
import dev.shivamnagpal.users.utils.RequestValidationUtils;
import dev.shivamnagpal.users.utils.RoutingConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;

public class RoleController {
    private final Router router;

    private final RoleService roleService;

    private final JWTService jwtService;

    public RoleController(
            Vertx vertx,
            Router mainRouter,
            RoleService roleService,
            JWTService jwtService
    ) {
        this.router = Router.router(vertx);
        this.roleService = roleService;
        this.jwtService = jwtService;
        mainRouter.mountSubRouter(RoutingConstants.ROLE, router);

        setupEndpoints();
    }

    private void setupEndpoints() {
        assignManagerRole();
        unAssignMangerRole();
    }

    private void assignManagerRole() {
        router.post(RoutingConstants.ASSIGN_MANAGER)
                .handler(routingContext -> {
                    String authToken = AuthenticationUtils.getAuthToken(routingContext.request());
                    JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                    jwtService.verifyRoles(jwtAuthTokenDTO, RoleName.ADMIN)
                            .compose(v -> RequestValidationUtils.fetchBodyAsJson(routingContext))
                            .compose(RoleUpdateRequestDTO::fromRequest)
                            .compose(
                                    roleUpdateRequestDTO -> roleService.assignManagerRole(
                                            roleUpdateRequestDTO.getUserId()
                                    )
                            )
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

    private void unAssignMangerRole() {
        router.post(RoutingConstants.UN_ASSIGN_MANAGER)
                .handler(routingContext -> {
                    String authToken = AuthenticationUtils.getAuthToken(routingContext.request());
                    JWTAuthTokenDTO jwtAuthTokenDTO = jwtService.decodeAuthToken(authToken);
                    jwtService.verifyRoles(jwtAuthTokenDTO, RoleName.ADMIN)
                            .compose(v -> RequestValidationUtils.fetchBodyAsJson(routingContext))
                            .compose(RoleUpdateRequestDTO::fromRequest)
                            .compose(
                                    roleUpdateRequestDTO -> roleService.unAssignManagerRole(
                                            roleUpdateRequestDTO.getUserId()
                                    )
                            )
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
