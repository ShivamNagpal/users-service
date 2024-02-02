package dev.shivamnagpal.users.enums;

import dev.shivamnagpal.users.utils.Constants;
import lombok.Getter;

@Getter
public enum ErrorCode {
    REQUEST_BODY_NOT_PROVIDED("001", "Request Body not provided"),
    USER_NOT_FOUND("002", "User not found"),
    VALIDATION_ERRORS_IN_THE_REQUEST("003", "Validation errors in the request"),
    USER_ALREADY_EXISTS("004", "User already exists"),
    INVALID_REFRESH_TOKEN("005", "Invalid refresh token"),
    INVALID_CREDENTIALS("006", "Invalid Credentials"),
    OTP_TOKEN_NOT_PROVIDED("007", "OTP Token not provided"),
    USER_IS_ALREADY_A_MANAGER("008", "User is already a manager"),
    NEW_PASSWORD_CANNOT_BE_SAME_AS_THE_OLD_PASSWORD("009", "New password cannot be same as the old password"),
    AUTHORIZATION_HEADER_NOT_PROVIDED("010", "Authorization header not provided"),
    AUTHORIZATION_TOKEN_MUST_START_WITH_THE_BEARER("011", "Authorization token must start with the Bearer"),
    USER_IS_NOT_A_MANAGER("012", "User is not a Manager"),
    NO_ACTIVE_TRIGGERED_OTP_FOUND("013", "No active triggered otp found"),
    INCORRECT_OTP("014", "Incorrect OTP"),
    OTP_RESEND_LIMIT_EXCEEDED("015", "OTP Resend Limit exceeded"),
    INVALID_OTP_TOKEN("016", "Invalid OTP Token"),
    INVALID_AUTH_TOKEN("017", "Invalid Auth Token"),
    USER_IS_NOT_AUTHORIZED_TO_ACCESS("018", "User is not authorized to access"),
    USER_ACCOUNT_IS_NOT_ACTIVE("019", "User account is not active"),
    SESSION_HAS_EXPIRED("020", "Session has expired"),
    SESSION_IS_NOT_ACTIVE("021", "Session is not active"),
    NEW_EMAIL_CANNOT_BE_SAME_AS_THE_OLD_EMAIL("022", "New email cannot be same as the old email"),
    USER_ACCOUNT_WASN_T_DEACTIVATED_OR_MARKED_FOR_DELETION(
            "023", "User account wasn't deactivated or marked for deletion"
    ),
    USER_ACCOUNT_IS_UNVERIFIED("024", "User account is unverified"),
    INTERNAL_SERVER_ERROR("025", "Internal Server Error"),
    ;

    private final String code;

    private final String message;

    ErrorCode(String code, String message) {
        this.code = Constants.ERROR_CODE_PREFIX + code;
        this.message = message;
    }
}
