package com.nagpal.shivam.workout_manager_user.controllers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class HealthController {

  public HealthController(Vertx vertx, Router mainRouter) {
    Router router = Router.router(vertx);
    mainRouter.mountSubRouter("/health", router);
    setupEndpoints(router);
  }

  private void setupEndpoints(Router router) {
    router.route().handler(routingContext -> {
      HttpResponseStatus okResponseStatus = HttpResponseStatus.OK;
      routingContext.response().setStatusCode(okResponseStatus.code()).end(okResponseStatus.reasonPhrase());
    });
  }
}
