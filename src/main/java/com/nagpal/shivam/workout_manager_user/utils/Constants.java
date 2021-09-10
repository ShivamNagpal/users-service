package com.nagpal.shivam.workout_manager_user.utils;

public class Constants {
    public static final String PATH = "path";
    public static final String SYS = "sys";
    public static final String ENV = "env";
    public static final String FILE = "file";
    public static final String CONFIG_JSON_PATH = "config.json";
    public static final String CONFIG_PROFILE_JSON_PATH = "config-{0}.json";
    public static final String SELECT_1 = "SELECT 1;";
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    public static final String ENV_KEY_VERTX_PROFILES_ACTIVE = "VERTX_PROFILES_ACTIVE";
    public static final String PROFILES_SEPARATOR_REGEX_PATTERN = "\\s*,\\s*";
    public static final String JDBC_POSTGRESQL_URI = "jdbc:postgresql://{0}:{1}/{2}?useSSL=false";
    public static final String AVAILABLE_PROCESSORS = "AVAILABLE_PROCESSORS";
    public static final String MAIL_HOST = "mail.host";
    public static final String MAIL_PORT = "mail.port";
    public static final String CONNECTION_STRING = "connection_string";
    public static final String DB_NAME = "db_name";
    public static final String PING = "ping";
    public static final String SESSION = "session";
    public static final String USER_ID = "userId";
    public static final String APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE = "content-type";
    public static final int OTP_RETRY_LIMIT = 5;
    public static final int OTP_BACKOFF_TIME = 30;
    public static final int OTP_EXPIRY_TIME = 5;
    public static final int BCRYPT_PASSWORD_LOG_ROUNDS = 10;
    public static final int BCRYPT_OTP_LOG_ROUNDS = 4;
    public static final String EMAIL = "email";
    public static final String PURPOSE = "purpose";

    private Constants() {
    }
}
