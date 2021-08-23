package com.nagpal.shivam.workout_manager_user.verticles;

import com.nagpal.shivam.workout_manager_user.controllers.HealthController;
import com.nagpal.shivam.workout_manager_user.enums.Configuration;
import com.nagpal.shivam.workout_manager_user.services.HealthService;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.text.MessageFormat;
import java.util.logging.Logger;

public class HttpVerticle extends AbstractVerticle {

    private final Logger logger = Logger.getLogger(HttpVerticle.class.getName());

    @Override
    public void start(Promise<Void> startPromise) {
        String startVerticleMessage =
                MessageFormat.format(MessageConstants.STARTING_VERTICLE, this.getClass().getSimpleName());
        logger.info(startVerticleMessage);
        this.setupHttpServer(vertx, this.config())
                .onSuccess(a -> startPromise.complete())
                .onFailure(startPromise::fail);
    }


    private Future<Void> setupHttpServer(Vertx vertx, JsonObject config) {
        Promise<Void> promise = Promise.promise();
        Router mainRouter = Router.router(vertx);
        vertx.createHttpServer()
                .requestHandler(mainRouter)
                .listen(config.getInteger(Configuration.SERVER_PORT.getKey()), http -> {
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
        HealthService healthService = new HealthService(vertx);
        new HealthController(vertx, mainRouter, healthService);
    }
}
