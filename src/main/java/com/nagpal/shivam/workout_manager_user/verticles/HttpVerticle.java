package com.nagpal.shivam.workout_manager_user.verticles;

import com.nagpal.shivam.workout_manager_user.controllers.HealthController;
import com.nagpal.shivam.workout_manager_user.enums.Configuration;
import com.nagpal.shivam.workout_manager_user.services.HealthService;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpVerticle extends AbstractVerticle {

    private static final Logger logger = Logger.getLogger(HttpVerticle.class.getName());

    public static Future<String> deploy(Vertx vertx, JsonObject config) {
        DeploymentOptions httpDeploymentOptions = new DeploymentOptions()
                .setInstances(config.getInteger(Constants.AVAILABLE_PROCESSORS))
                .setConfig(config);
        return vertx.deployVerticle(HttpVerticle.class.getName(), httpDeploymentOptions)
                .onSuccess(result -> logger.log(Level.INFO,
                        MessageFormat.format(MessageConstants.SERVER_STARTED_ON_PORT,
                                String.valueOf(config.getInteger(Configuration.SERVER_PORT.getKey())))));
    }

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
