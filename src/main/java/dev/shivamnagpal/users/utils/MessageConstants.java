package dev.shivamnagpal.users.utils;

public class MessageConstants {
    public static final String SERVER_STARTED_ON_PORT = "Server started on port: {0}";

    public static final String MANDATORY_CONFIGS_ARE_NOT_FOUND = "Mandatory Configs are not found: {0}";

    public static final String SUCCESSFULLY_DEPLOYED_THE_VERTICLES = "Successfully deployed the Verticles";

    public static final String STARTING_VERTICLE = "Starting verticle \"{0}\"";

    public static final String SHUTTING_DOWN_THE_VERT_X = "Shutting down the Vert.x";

    public static final String SUCCESSFULLY_CREATED_SQL_CLIENT_INSTANCE = "Successfully created SqlClient instance";

    public static final String DUPLICATE_CONFIG_KEYS_PROVIDED = "Duplicate config keys provided: {0}";

    public static final String VERTX_ACTIVE_PROFILES = "Vertx active profiles: {0}";

    public static final String MUST_NOT_BE_A_NULL_VALUE = "Must not be a null value";

    public static final String MUST_NOT_BE_BLANK = "Must not be blank";

    public static final String PG_POOL_HEALTH_CHECK_FAILED = "PG Pool Health Check Failed";

    public static final String MONGO_CLIENT_HEALTH_CHECK_FAILED = "Mongo Client Health Check Failed";

    public static final String OTP_EMAIL_SUBJECT = "OTP for verification";

    public static final String OTP_EMAIL_CONTENT_FORMAT = "Please use this OTP {0}";

    public static final String POST_VERIFICATION_ACTION_NOT_MAPPED_FOR_THE_OTP_PURPOSE = "Post verification action not mapped for the OTP Purpose: {0}";

    public static final String USER_ACCOUNT_IS_UNVERIFIED = "User account is unverified";

    public static final String QUERY_PARAM_MUST_HAVE_ONLY_BOOLEAN_VALUES = "Query param: {0} must have only boolean values";

    public static final String DELETION_CRON_FAILED = "Deletion Cron failed";

    public static final String DELETION_CRON_EXECUTED_SUCCESSFULLY = "Deletion Cron executed successfully";

    private MessageConstants() {
    }
}
