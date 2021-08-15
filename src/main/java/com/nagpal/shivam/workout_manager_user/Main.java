package com.nagpal.shivam.workout_manager_user;

import com.nagpal.shivam.workout_manager_user.exceptions.AppException;
import com.nagpal.shivam.workout_manager_user.utils.ConfigurationConstants;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import com.nagpal.shivam.workout_manager_user.verticles.MainVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
  private static final Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    int availableProcessors = Runtime.getRuntime().availableProcessors();
    getConfiguration(vertx)
      .compose(config -> {
        String[] missingConfigs = validateMandatoryConfigs(config);
        if (missingConfigs.length != 0) {
          String message =
            MessageFormat.format(MessageConstants.MANDATORY_CONFIGS_ARE_NOT_FOUND, Arrays.toString(missingConfigs));
          logger.log(Level.SEVERE, message);
          return Future.failedFuture(new AppException(message));
        }
        DeploymentOptions mainDeploymentOptions = new DeploymentOptions()
          .setInstances(availableProcessors)
          .setConfig(config);
        return vertx.deployVerticle(MainVerticle.class.getName(), mainDeploymentOptions)
          .onSuccess(result -> logger.log(Level.INFO, MessageFormat.format(MessageConstants.SERVER_STARTED_ON_PORT,
            String.valueOf(config.getInteger(ConfigurationConstants.SERVER_PORT)))));
      })
      .onSuccess(result -> {
        logger.log(Level.INFO, MessageConstants.SUCCESSFULLY_DEPLOYED_THE_VERTICLES);
      })
      .onFailure(throwable -> {
        logger.log(Level.SEVERE, throwable.getMessage(), throwable);
        vertx.close();
      });
  }

  private static Future<JsonObject> getConfiguration(Vertx vertx) {
    ConfigStoreOptions configFileOptions = new ConfigStoreOptions()
      .setType(Constants.FILE)
      .setConfig(new JsonObject().put(Constants.PATH, ConfigurationConstants.CONFIG_JSON_PATH));

    ConfigStoreOptions configSysOptions = new ConfigStoreOptions()
      .setType(Constants.SYS);

    ConfigStoreOptions configEnvOptions = new ConfigStoreOptions()
      .setType(Constants.ENV);

    ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
      .addStore(configFileOptions)
      .addStore(configSysOptions)
      .addStore(configEnvOptions);

    ConfigRetriever configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);
    return configRetriever.getConfig();
  }

  private static String[] validateMandatoryConfigs(JsonObject config) {
    LinkedList<String> keys = new LinkedList<>();
    for (String key : ConfigurationConstants.MANDATORY_CONFIGS) {
      if (!config.containsKey(key)) {
        keys.add(key);
      }
    }
    return keys.toArray(new String[0]);
  }
}
