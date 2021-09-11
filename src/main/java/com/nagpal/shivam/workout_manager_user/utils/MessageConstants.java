package com.nagpal.shivam.workout_manager_user.utils;

public class MessageConstants {
    public static final String SERVER_STARTED_ON_PORT = "Server started on port: {0}";
    public static final String MANDATORY_CONFIGS_ARE_NOT_FOUND = "Mandatory Configs are not found: {0}";
    public static final String SUCCESSFULLY_DEPLOYED_THE_VERTICLES = "Successfully deployed the Verticles";
    public static final String STARTING_VERTICLE = "Starting verticle \"{0}\"";
    public static final String SHUTTING_DOWN_THE_VERT_X = "Shutting down the Vert.x";
    public static final String SUCCESSFULLY_CREATED_SQL_CLIENT_INSTANCE = "Successfully created SqlClient instance";
    public static final String DUPLICATE_CONFIG_KEYS_PROVIDED = "Duplicate config keys provided: {0}";
    public static final String VERTX_ACTIVE_PROFILES = "Vertx active profiles: {0}";
    public static final String RESPONSE_EXCEPTION_PAYLOAD = "ResponseException Payload: {0}";
    public static final String MUST_NOT_BE_A_NULL_VALUE = "Must not be a null value";
    public static final String MUST_NOT_BE_BLANK = "Must not be blank";
    public static final String VALIDATION_ERRORS_IN_THE_REQUEST = "Validation errors in the request";
    public static final String REQUEST_BODY_NOT_PROVIDED = "Request Body not provided";
    public static final String PG_POOL_HEALTH_CHECK_FAILED = "PG Pool Health Check Failed";
    public static final String MONGO_CLIENT_HEALTH_CHECK_FAILED = "Mongo Client Health Check Failed";
    public static final String OTP_EMAIL_SUBJECT = "OTP From Workout Manager";
    public static final String OTP_EMAIL_CONTENT_FORMAT = "Please use this OTP {0}";
    public static final String OTP_TOKEN_NOT_PROVIDED = "OTP Token not provided";
    public static final String INVALID_OTP_TOKEN = "Invalid OTP Token";
    public static final String OTP_RESEND_LIMIT_EXCEEDED = "OTP Limit exceeded";
    public static final String NO_ACTIVE_TRIGGERED_OTP_FOUND = "No active triggered otp found";
    public static final String INCORRECT_OTP = "Incorrect OTP";
    public static final String POST_VERIFICATION_ACTION_NOT_MAPPED_FOR_THE_OTP_PURPOSE =
            "Post verification action not mapped for the OTP Purpose: {0}";

    private MessageConstants() {
    }
}
