package com.nagpal.shivam.workout_manager_user.utils;

import com.nagpal.shivam.workout_manager_user.enums.Configuration;
import com.nagpal.shivam.workout_manager_user.exceptions.AppException;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ConfigurationUtils {
    private static final Logger logger = Logger.getLogger(ConfigurationUtils.class.getName());

    private ConfigurationUtils() {
    }

    public static Future<JsonObject> getConfiguration(Vertx vertx) {
        ConfigStoreOptions configFileOptions = new ConfigStoreOptions()
                .setType(Constants.FILE)
                .setConfig(new JsonObject().put(Constants.PATH, Constants.CONFIG_JSON_PATH));

        ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
                .addStore(configFileOptions);

        String[] profiles = checkForConfigurationProfiles();
        if (profiles.length != 0) {
            String message = MessageFormat.format(MessageConstants.VERTX_ACTIVE_PROFILES, Arrays.toString(profiles));
            logger.log(Level.INFO, message);
            for (String profile : profiles) {
                String path = MessageFormat.format(Constants.CONFIG_PROFILE_JSON_PATH, profile);
                ConfigStoreOptions configProfileFileOptions = new ConfigStoreOptions()
                        .setType(Constants.FILE)
                        .setOptional(true)
                        .setConfig(new JsonObject().put(Constants.PATH, path));
                configRetrieverOptions.addStore(configProfileFileOptions);
            }
        }

        ConfigStoreOptions configEnvOptions = new ConfigStoreOptions()
                .setType(Constants.ENV);

        ConfigStoreOptions configSysOptions = new ConfigStoreOptions()
                .setType(Constants.SYS);

        configRetrieverOptions.addStore(configEnvOptions)
                .addStore(configSysOptions);

        ConfigRetriever configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);
        return configRetriever.getConfig();
    }

    private static String[] checkForConfigurationProfiles() {
        Map<String, String> propertiesKeyMap = System.getProperties().keySet().stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toMap(String::toLowerCase, Function.identity()));

        String[] profiles = new String[0];
        if (propertiesKeyMap.containsKey(Constants.SYS_KEY_VERTX_PROFILES_ACTIVE)) {
            profiles = System.getProperty(propertiesKeyMap.get(Constants.SYS_KEY_VERTX_PROFILES_ACTIVE))
                    .split(Constants.PROFILES_SEPARATOR_REGEX_PATTERN);
        } else {
            Map<String, String> envMap = System.getenv().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().toUpperCase(), Map.Entry::getValue));
            if (envMap.containsKey(Constants.ENV_KEY_VERTX_PROFILES_ACTIVE)) {
                profiles = envMap.get(Constants.ENV_KEY_VERTX_PROFILES_ACTIVE)
                        .split(Constants.PROFILES_SEPARATOR_REGEX_PATTERN);
            }
        }

        return profiles;
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
