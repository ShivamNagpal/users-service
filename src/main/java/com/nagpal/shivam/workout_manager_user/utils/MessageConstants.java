package com.nagpal.shivam.workout_manager_user.utils;

public class MessageConstants {
    public static final String SERVER_STARTED_ON_PORT = "Server started on port: {0}";
    public static final String MANDATORY_CONFIGS_ARE_NOT_FOUND = "Mandatory Configs are not found: {0}";
    public static final String SUCCESSFULLY_DEPLOYED_THE_VERTICLES = "Successfully deployed the Verticles";
    public static final String STARTING_VERTICLE = "Starting verticle \"{0}\"";
    public static final String SHUTTING_DOWN_THE_VERT_X = "Shutting down the Vert.x";
    public static final String SUCCESSFULLY_CONNECTED_TO_THE_POSTGRESQL_DATABASE =
            "Successfully connected to the Postgresql Database";
    public static final String SUCCESSFULLY_CREATED_SQL_CLIENT_INSTANCE = "Successfully created SqlClient instance";
    public static final String DUPLICATE_CONFIG_KEYS_PROVIDED = "Duplicate config keys provided: {0}";
    public static final String VERTX_ACTIVE_PROFILES = "Vertx active profiles: {0}";
    public static final String RESPONSE_EXCEPTION_PAYLOAD = "ResponseException Payload: {0}";

    private MessageConstants() {
    }
}
