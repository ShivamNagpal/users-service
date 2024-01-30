package dev.shivamnagpal.users;

import dev.shivamnagpal.users.configurations.DatabaseConfiguration;
import dev.shivamnagpal.users.exceptions.AppException;
import dev.shivamnagpal.users.utils.ConfigurationUtils;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.MessageConstants;
import dev.shivamnagpal.users.utils.PeriodicTasksUtil;
import dev.shivamnagpal.users.verticles.MainVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Arrays;

@Slf4j
public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ConfigurationUtils.getConfiguration(vertx)
                .compose(config -> {
                    String[] missingConfigs = ConfigurationUtils.validateMandatoryConfigs(config);
                    if (missingConfigs.length != 0) {
                        String message = MessageFormat.format(
                                MessageConstants.MANDATORY_CONFIGS_ARE_NOT_FOUND,
                                Arrays.toString(missingConfigs)
                        );
                        return Future.failedFuture(new AppException(message));
                    }
                    config.put(Constants.AVAILABLE_PROCESSORS, availableProcessors);
                    return vertx.executeBlocking(() -> {
                        DatabaseConfiguration.initFlyway(config);
                        return null;
                    })
                            .compose(o -> DatabaseConfiguration.verifyMongoIndices(vertx, config))
                            .compose(result -> MainVerticle.deploy(vertx, config))
                            .compose(result -> PeriodicTasksUtil.setupPeriodicTasks(vertx, config));
                })
                .onSuccess(result -> log.info(MessageConstants.SUCCESSFULLY_DEPLOYED_THE_VERTICLES))
                .onFailure(throwable -> {
                    log.error(throwable.getMessage(), throwable);
                    vertx.close(ar -> System.exit(-1));
                });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                log.info(MessageConstants.SHUTTING_DOWN_THE_VERT_X);
                vertx.close();
            }
        });
    }

}
