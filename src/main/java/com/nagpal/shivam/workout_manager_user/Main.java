package com.nagpal.shivam.workout_manager_user;

import com.nagpal.shivam.workout_manager_user.configurations.DatabaseConfiguration;
import com.nagpal.shivam.workout_manager_user.exceptions.AppException;
import com.nagpal.shivam.workout_manager_user.utils.ConfigurationUtils;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import com.nagpal.shivam.workout_manager_user.verticles.MainVerticle;
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
                    String[] missingConfigs = ConfigurationUtils.validateMandatoryConfigs(config);
                    if (missingConfigs.length != 0) {
                        String message =
                                MessageFormat.format(MessageConstants.MANDATORY_CONFIGS_ARE_NOT_FOUND,
                                        Arrays.toString(missingConfigs));
                        return Future.failedFuture(new AppException(message));
                    }
                    config.put(Constants.AVAILABLE_PROCESSORS, availableProcessors);
                    return vertx.executeBlocking(promise -> {
                                DatabaseConfiguration.initFlyway(config);
                                promise.complete();
                            })
                            .compose(o -> DatabaseConfiguration.verifyMongoIndices(vertx, config))
                            .compose(result -> MainVerticle.deploy(vertx, config)
                            );
                })
                .onSuccess(result -> logger.log(Level.INFO, MessageConstants.SUCCESSFULLY_DEPLOYED_THE_VERTICLES))
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
