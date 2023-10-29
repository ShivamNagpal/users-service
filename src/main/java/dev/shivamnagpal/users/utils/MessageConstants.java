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
    public static final String RESPONSE_EXCEPTION_PAYLOAD = "ResponseException Payload: {0}";
    public static final String MUST_NOT_BE_A_NULL_VALUE = "Must not be a null value";
    public static final String MUST_NOT_BE_BLANK = "Must not be blank";
    public static final String VALIDATION_ERRORS_IN_THE_REQUEST = "Validation errors in the request";
    public static final String REQUEST_BODY_NOT_PROVIDED = "Request Body not provided";
    public static final String PG_POOL_HEALTH_CHECK_FAILED = "PG Pool Health Check Failed";
    public static final String MONGO_CLIENT_HEALTH_CHECK_FAILED = "Mongo Client Health Check Failed";
    public static final String OTP_EMAIL_SUBJECT = "OTP for verification";
    public static final String OTP_EMAIL_CONTENT_FORMAT = "Please use this OTP {0}";
    public static final String OTP_TOKEN_NOT_PROVIDED = "OTP Token not provided";
    public static final String INVALID_OTP_TOKEN = "Invalid OTP Token";
    public static final String OTP_RESEND_LIMIT_EXCEEDED = "OTP Resend Limit exceeded";
    public static final String NO_ACTIVE_TRIGGERED_OTP_FOUND = "No active triggered otp found";
    public static final String INCORRECT_OTP = "Incorrect OTP";
    public static final String POST_VERIFICATION_ACTION_NOT_MAPPED_FOR_THE_OTP_PURPOSE =
            "Post verification action not mapped for the OTP Purpose: {0}";
    public static final String INVALID_AUTH_TOKEN = "Invalid Auth Token";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String INVALID_CREDENTIALS = "Invalid Credentials";
    public static final String USER_ACCOUNT_IS_NOT_ACTIVE = "User account is not active";
    public static final String USER_ACCOUNT_IS_UNVERIFIED = "User account is unverified";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String SESSION_HAS_EXPIRED = "Session has expired";
    public static final String SESSION_IS_NOT_ACTIVE = "Session is not active";
    public static final String AUTHORIZATION_TOKEN_MUST_START_WITH_THE_BEARER =
            "Authorization token must start with the Bearer";
    public static final String AUTHORIZATION_HEADER_NOT_PROVIDED = "Authorization header not provided";
    public static final String QUERY_PARAM_MUST_HAVE_ONLY_BOOLEAN_VALUES =
            "Query param: {0} must have only boolean values";
    public static final String NEW_EMAIL_CANNOT_BE_SAME_AS_THE_OLD_EMAIL = "New email cannot be same as the old email";
    public static final String NEW_PASSWORD_CANNOT_BE_SAME_AS_THE_OLD_PASSWORD =
            "New password cannot be same as the old password";
    public static final String USER_ACCOUNT_WASN_T_DEACTIVATED_OR_MARKED_FOR_DELETION =
            "User account wasn't deactivated or marked for deletion";
    public static final String USER_IS_NOT_AUTHORIZED_TO_ACCESS = "User is not authorized to access";
    public static final String USER_IS_ALREADY_A_MANAGER = "User is already a manager";
    public static final String USER_IS_NOT_A_MANAGER = "User is not a Manager";
    public static final String DELETION_CRON_FAILED = "Deletion Cron failed";
    public static final String DELETION_CRON_EXECUTED_SUCCESSFULLY = "Deletion Cron executed successfully";

    private MessageConstants() {
    }
}
