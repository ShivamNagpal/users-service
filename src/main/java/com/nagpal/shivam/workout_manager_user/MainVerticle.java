package com.nagpal.shivam.workout_manager_user;

import com.nagpal.shivam.workout_manager_user.utils.ConfigurationConstants;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

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
        return Future.failedFuture("Mandatory Configs are not found: " + Arrays.toString(missingConfigs));
      }
      return this.setupHttpServer(vertx, config);
    }).onSuccess(a -> startPromise.complete()).onFailure(startPromise::fail);
  }

  private Future<JsonObject> getConfiguration(Vertx vertx) {
    ConfigStoreOptions configFileOptions =
      new ConfigStoreOptions().setType("file").setConfig(new JsonObject().put("path", "conf/config.json"));

    ConfigStoreOptions configSysOptions = new ConfigStoreOptions().setType("sys");

    ConfigStoreOptions configEnvOptions = new ConfigStoreOptions().setType("env");

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
    vertx.createHttpServer()
      .requestHandler(req -> req.response().putHeader("content-type", "text/plain").end("Hello from Vert.x!"))
      .listen(serverPort, http -> {
        if (http.succeeded()) {
          logger.log(Level.INFO,
            MessageFormat.format(ConfigurationConstants.SERVER_STARTED_ON_PORT, String.valueOf(serverPort)));
          promise.complete();
        } else {
          promise.fail(http.cause());
        }
      });
    return promise.future();
  }
}
