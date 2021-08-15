package com.nagpal.shivam.workout_manager_user;

import com.nagpal.shivam.workout_manager_user.controllers.HealthController;
import com.nagpal.shivam.workout_manager_user.exceptions.AppException;
import com.nagpal.shivam.workout_manager_user.utils.ConfigurationConstants;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainVerticle extends AbstractVerticle {

  private final Logger logger = Logger.getLogger(MainVerticle.class.getName());

  @Override
  public void start(Promise<Void> startPromise) {
    this.getConfiguration(vertx).compose(config -> {
      String[] missingConfigs = validateMandatoryConfigs(config);
      if (missingConfigs.length != 0) {
        String message =
          MessageFormat.format(MessageConstants.MANDATORY_CONFIGS_ARE_NOT_FOUND, Arrays.toString(missingConfigs));
        logger.log(Level.SEVERE, message);
        return Future.failedFuture(new AppException(message));
      }
      return this.setupHttpServer(vertx, config);
    }).onSuccess(a -> startPromise.complete()).onFailure(startPromise::fail);
  }

  private Future<JsonObject> getConfiguration(Vertx vertx) {
    ConfigStoreOptions configFileOptions = new ConfigStoreOptions().setType("file")
      .setConfig(new JsonObject().put(Constants.PATH, ConfigurationConstants.CONFIG_JSON_PATH));

    ConfigStoreOptions configSysOptions = new ConfigStoreOptions().setType(Constants.SYS);

    ConfigStoreOptions configEnvOptions = new ConfigStoreOptions().setType(Constants.ENV);

    ConfigRetrieverOptions configRetrieverOptions =
      new ConfigRetrieverOptions().addStore(configFileOptions).addStore(configSysOptions).addStore(configEnvOptions);

    ConfigRetriever configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);
    return configRetriever.getConfig();
  }

  private String[] validateMandatoryConfigs(JsonObject config) {
    LinkedList<String> keys = new LinkedList<>();
    for (String key : ConfigurationConstants.MANDATORY_CONFIGS) {
      Object value = config.getValue(key);
      if (null == value) {
        keys.add(key);
      }
    }
    return keys.toArray(new String[0]);
  }

  private Future<Void> setupHttpServer(Vertx vertx, JsonObject config) {
    Promise<Void> promise = Promise.promise();
    Integer serverPort = config.getInteger(ConfigurationConstants.SERVER_PORT);
    Router mainRouter = Router.router(vertx);
    vertx.createHttpServer().requestHandler(mainRouter).listen(serverPort, http -> {
      if (http.succeeded()) {
        logger.log(Level.INFO,
          MessageFormat.format(MessageConstants.SERVER_STARTED_ON_PORT, String.valueOf(serverPort)));
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
