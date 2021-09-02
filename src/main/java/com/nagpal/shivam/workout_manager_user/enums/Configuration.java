package com.nagpal.shivam.workout_manager_user.enums;

import com.nagpal.shivam.workout_manager_user.utils.Constants;
import io.vertx.core.json.JsonObject;

import java.util.List;

public enum Configuration {
    VERTX_PROFILES_ACTIVE("vertx.profiles.active", Constants.ENV_KEY_VERTX_PROFILES_ACTIVE),
    SERVER_PORT("server.port", "SERVER_PORT"),
    PG_HOST("pg.host", "PG_HOST"),
    PG_PORT("pg.port", "PG_PORT"),
    PG_DATABASE("pg.database", "PG_DATABASE"),
    PG_USERNAME("pg.username", "PG_USERNAME"),
    PG_PASSWORD("pg.password", "PG_PASSWORD"),
    MAIL_USERNAME("mail.username", "MAIL_USERNAME"),
    MAIL_PASSWORD("mail.password", "MAIL_PASSWORD"),
    ;
    public static final List<Configuration> MANDATORY_CONFIGURATIONS =
            List.of(SERVER_PORT, PG_HOST, PG_PORT, PG_DATABASE, PG_USERNAME, PG_PASSWORD, MAIL_USERNAME, MAIL_PASSWORD);
    private final String sysKey;
    private final String envKey;

    Configuration(String sysKey, String envKey) {
        this.sysKey = sysKey.toLowerCase();
        this.envKey = envKey.toUpperCase();
    }

    public static void normalizeConfigurationKey(JsonObject config, Configuration configuration) {
        if (!config.containsKey(configuration.sysKey) && config.containsKey(configuration.envKey)) {
            config.put(configuration.sysKey, config.getValue(configuration.envKey));
        }
    }

    public String getKey() {
        return sysKey;
    }
}
