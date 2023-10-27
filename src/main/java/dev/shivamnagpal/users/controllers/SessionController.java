package dev.shivamnagpal.users.controllers;

import dev.shivamnagpal.users.dtos.request.RefreshSessionRequestDTO;
import dev.shivamnagpal.users.dtos.response.ResponseWrapper;
import dev.shivamnagpal.users.exceptions.handlers.GlobalExceptionHandler;
import dev.shivamnagpal.users.services.SessionService;
import dev.shivamnagpal.users.utils.RequestValidationUtils;
import dev.shivamnagpal.users.utils.RoutingConstants;
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
