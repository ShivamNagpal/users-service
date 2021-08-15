package com.nagpal.shivam.workout_manager_user.verticles;

import com.nagpal.shivam.workout_manager_user.controllers.HealthController;
import com.nagpal.shivam.workout_manager_user.utils.ConfigurationConstants;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.text.MessageFormat;
import java.util.logging.Logger;

public class MainVerticle extends AbstractVerticle {

  private final Logger logger = Logger.getLogger(MainVerticle.class.getName());

  @Override
  public void start(Promise<Void> startPromise) {
    logger.info(MessageFormat.format(MessageConstants.STARTING_VERTICLE, this.getClass().getSimpleName()));
    this.setupHttpServer(vertx, this.config())
      .onSuccess(a -> startPromise.complete())
      .onFailure(startPromise::fail);
  }


  private Future<Void> setupHttpServer(Vertx vertx, JsonObject config) {
    Promise<Void> promise = Promise.promise();
    Router mainRouter = Router.router(vertx);
    vertx.createHttpServer()
      .requestHandler(mainRouter)
      .listen(config.getInteger(ConfigurationConstants.SERVER_PORT), http -> {
        if (http.succeeded()) {
          initComponents(vertx, mainRouter);
          promise.complete();
        } else {
          promise.fail(http.cause());
        }
      });
    return promise.future();
  }

  private void initComponents(Vertx vertx, Router mainRouter) {
    new HealthController(vertx, mainRouter);
  }
}
