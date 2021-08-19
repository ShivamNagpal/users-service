package com.nagpal.shivam.workout_manager_user;

import com.nagpal.shivam.workout_manager_user.enums.Configuration;
import com.nagpal.shivam.workout_manager_user.exceptions.AppException;
import com.nagpal.shivam.workout_manager_user.utils.ConfigurationUtils;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import com.nagpal.shivam.workout_manager_user.verticles.DatabaseVerticle;
import com.nagpal.shivam.workout_manager_user.verticles.HttpVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ConfigurationUtils.getConfiguration(vertx)
                .compose(config -> {
                    ConfigurationUtils.correctConfigCasing(config);
                    ConfigurationUtils.normalizeKeys(config);
                    String[] missingConfigs = ConfigurationUtils.validateMandatoryConfigs(config);
                    if (missingConfigs.length != 0) {
                        String message =
                                MessageFormat.format(MessageConstants.MANDATORY_CONFIGS_ARE_NOT_FOUND,
                                        Arrays.toString(missingConfigs));
                        return Future.failedFuture(new AppException(message));
                    }
                    DeploymentOptions httpDeploymentOptions = new DeploymentOptions()
                            .setInstances(availableProcessors)
                            .setConfig(config);
                    return vertx.deployVerticle(HttpVerticle.class.getName(), httpDeploymentOptions)
                            .onSuccess(result -> logger.log(Level.INFO,
                                    MessageFormat.format(MessageConstants.SERVER_STARTED_ON_PORT,
                                            String.valueOf(config.getInteger(Configuration.SERVER_PORT.getKey())))))
                            .compose(result -> {
                                DeploymentOptions databaseDeploymentOptions = new DeploymentOptions()
                                        .setInstances(availableProcessors)
                                        .setConfig(config);
                                return vertx.deployVerticle(DatabaseVerticle.class.getName(),
                                                databaseDeploymentOptions)
                                        .onSuccess(dbDepId -> logger.log(Level.INFO,
                                                MessageConstants.SUCCESSFULLY_CONNECTED_TO_THE_POSTGRESQL_DATABASE));
                            });
                })
                .onSuccess(result -> {
                    logger.log(Level.INFO, MessageConstants.SUCCESSFULLY_DEPLOYED_THE_VERTICLES);
                })
                .onFailure(throwable -> {
                    logger.log(Level.SEVERE, throwable.getMessage(), throwable);
                    vertx.close(ar -> System.exit(-1));
                });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                logger.log(Level.INFO, MessageConstants.SHUTTING_DOWN_THE_VERT_X);
                vertx.close();
            }
        });
    }
}
