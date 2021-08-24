package com.nagpal.shivam.workout_manager_user.utils;

import com.nagpal.shivam.workout_manager_user.enums.Configuration;
import com.nagpal.shivam.workout_manager_user.exceptions.AppException;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.LinkedList;

public class ConfigurationUtils {

    private ConfigurationUtils() {
    }

    public static Future<JsonObject> getConfiguration(Vertx vertx) {
        ConfigStoreOptions configFileOptions = new ConfigStoreOptions()
                .setType(Constants.FILE)
                .setConfig(new JsonObject().put(Constants.PATH, Constants.CONFIG_JSON_PATH));

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

    public static void correctConfigCasing(JsonObject config) {
        new LinkedList<>(config.fieldNames()).forEach(key -> {
            Object value = config.getValue(key);
            String newKey;
            if (key.contains(".")) {
                newKey = key.toLowerCase();
            } else if (key.contains("_")) {
                newKey = key.toUpperCase();
            } else {
                newKey = key;
            }
            config.remove(key);
            if (config.containsKey(newKey)) {
                throw new AppException(MessageConstants.DUPLICATE_CONFIG_KEYS_PROVIDED);
            }
            config.put(newKey, value);
        });
    }

    public static void normalizeKeys(JsonObject config) {
        for (Configuration configuration : Configuration.values()) {
            Configuration.normalizeConfigurationKey(config, configuration);
        }
    }

    public static String[] validateMandatoryConfigs(JsonObject config) {
        LinkedList<String> keys = new LinkedList<>();
        for (Configuration configuration : Configuration.MANDATORY_CONFIGURATIONS) {
            if (!config.containsKey(configuration.getKey())) {
                keys.add(configuration.getKey());
            }
        }
        return keys.toArray(new String[0]);
    }
}
