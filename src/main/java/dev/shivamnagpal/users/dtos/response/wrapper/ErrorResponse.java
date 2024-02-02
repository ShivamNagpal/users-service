package dev.shivamnagpal.users.dtos.response.wrapper;

import dev.shivamnagpal.users.enums.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ErrorResponse {

    private final String errorCode;

    private final String message;

    private final String detail;

    private final String help;

    public static ErrorResponse from(ErrorCode errorCode) {
        return from(errorCode, null, null);
    }

    public static ErrorResponse from(ErrorCode errorCode, String detail) {
        return from(errorCode, detail, null);
    }

    public static ErrorResponse from(ErrorCode errorCode, String detail, String help) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), detail, help);
    }
}
