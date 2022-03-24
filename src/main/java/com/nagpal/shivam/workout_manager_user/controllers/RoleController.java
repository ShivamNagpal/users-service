package com.nagpal.shivam.workout_manager_user.controllers;

import com.nagpal.shivam.workout_manager_user.dtos.internal.JWTAuthTokenDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.RoleUpdateRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.ResponseWrapper;
import com.nagpal.shivam.workout_manager_user.enums.RoleName;
import com.nagpal.shivam.workout_manager_user.exceptions.handlers.GlobalExceptionHandler;
import com.nagpal.shivam.workout_manager_user.services.JWTService;
import com.nagpal.shivam.workout_manager_user.services.RoleService;
import com.nagpal.shivam.workout_manager_user.utils.AuthenticationUtils;
import com.nagpal.shivam.workout_manager_user.utils.RequestValidationUtils;
import com.nagpal.shivam.workout_manager_user.utils.RoutingConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;

public class RoleController {
    private final Router router;
    private final RoleService roleService;
    private final JWTService jwtService;

    public RoleController(Vertx vertx, Router mainRouter, RoleService roleService,
                          JWTService jwtService) {
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
                            .compose(roleUpdateRequestDTO -> roleService.assignManagerRole(
                                    roleUpdateRequestDTO.getUserId())
                            )
                            .onSuccess(obj -> routingContext.response()
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                            )
                            .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
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
                            .compose(roleUpdateRequestDTO -> roleService.unAssignManagerRole(
                                    roleUpdateRequestDTO.getUserId())
                            )
                            .onSuccess(obj -> routingContext.response()
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                            )
                            .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                            );
                });
    }
}
