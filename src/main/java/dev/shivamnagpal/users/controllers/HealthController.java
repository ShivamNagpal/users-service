package dev.shivamnagpal.users.controllers;

import dev.shivamnagpal.users.core.Controller;
import dev.shivamnagpal.users.core.RequestPath;
import dev.shivamnagpal.users.exceptions.handlers.GlobalExceptionHandler;
import dev.shivamnagpal.users.services.HealthService;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.RoutingConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.Router;

public class HealthController extends Controller {

    private final HealthService healthService;

    public HealthController(Router router, RequestPath requestPath, HealthService healthService) {
        super(router, requestPath.next(RoutingConstants.HEALTH));
        this.healthService = healthService;
    }

    @Override
    public void registerRoutes() {
        appHealth();
        dbHealth();
    }

    private void appHealth() {
        super.router.route(super.requestPath.path())
                .handler(
                        routingContext -> routingContext.response()
                                .setStatusCode(HttpResponseStatus.OK.code())
                                .end(Constants.UP)
                );
    }

    private void dbHealth() {
        super.router.route(super.requestPath.next(RoutingConstants.DB).path())
                .handler(
                        routingContext -> healthService.checkDbHealth()
                                .onSuccess(
                                        message -> routingContext.response()
                                                .setStatusCode(HttpResponseStatus.OK.code())
                                                .end(message)
                                )
                                .onFailure(
                                        throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response())
                                )
                );
    }
}
