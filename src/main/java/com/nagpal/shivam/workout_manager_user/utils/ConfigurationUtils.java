package com.nagpal.shivam.workout_manager_user.utils;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.LinkedList;

public class ConfigurationUtils {

    public static Future<JsonObject> getConfiguration(Vertx vertx) {
        ConfigStoreOptions configFileOptions = new ConfigStoreOptions()
                .setType(Constants.FILE)
                .setConfig(new JsonObject().put(Constants.PATH, ConfigurationConstants.CONFIG_JSON_PATH));

        ConfigStoreOptions configEnvOptions = new ConfigStoreOptions()
                .setType(Constants.ENV);

        ConfigStoreOptions configSysOptions = new ConfigStoreOptions()
                .setType(Constants.SYS);

        ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
                .addStore(configFileOptions)
                .addStore(configEnvOptions)
                .addStore(configSysOptions);

        ConfigRetriever configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);
        return configRetriever.getConfig();
    }

    public static String[] validateMandatoryConfigs(JsonObject config) {
        LinkedList<String> keys = new LinkedList<>();
        for (String key : ConfigurationConstants.MANDATORY_CONFIGS) {
            if (!config.containsKey(key)) {
                keys.add(key);
            }
        }
        return keys.toArray(new String[0]);
    }
}
