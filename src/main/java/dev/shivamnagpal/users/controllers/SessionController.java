package dev.shivamnagpal.users.controllers;

import dev.shivamnagpal.users.core.Controller;
import dev.shivamnagpal.users.core.RequestPath;
import dev.shivamnagpal.users.dtos.request.RefreshSessionRequestDTO;
import dev.shivamnagpal.users.dtos.response.wrapper.ResponseWrapper;
import dev.shivamnagpal.users.exceptions.handlers.GlobalExceptionHandler;
import dev.shivamnagpal.users.services.SessionService;
import dev.shivamnagpal.users.utils.RequestValidationUtils;
import dev.shivamnagpal.users.utils.RoutingConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;

public class SessionController extends Controller {

    private final SessionService sessionService;

    public SessionController(Router router, RequestPath requestPath, SessionService sessionService) {
        super(router, requestPath.next(RoutingConstants.SESSION));
        this.sessionService = sessionService;
    }

    @Override
    public void registerRoutes() {
        refreshToken();
    }

    private void refreshToken() {
        super.router.post(super.requestPath.next(RoutingConstants.REFRESH_TOKEN).path())
                .handler(
                        routingContext -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                                .compose(RefreshSessionRequestDTO::fromRequest)
                                .compose(sessionService::refreshSession)
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

}
