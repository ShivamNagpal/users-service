package dev.shivamnagpal.users.exceptions;

import dev.shivamnagpal.users.dtos.response.wrapper.ErrorResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class RestException extends AppException {
    private final HttpResponseStatus httpStatus;

    private final List<ErrorResponse> errorResponses;

    public RestException(HttpResponseStatus httpStatus, ErrorResponse errorResponse) {
        this(httpStatus, errorResponse, null);
    }

    public RestException(HttpResponseStatus httpStatus, ErrorResponse errorResponse, Throwable cause) {
        this(httpStatus, List.of(errorResponse), cause);
    }

    public RestException(HttpResponseStatus httpStatus, List<ErrorResponse> errorResponses) {
        this(httpStatus, errorResponses, null);
    }

    public RestException(HttpResponseStatus httpStatus, List<ErrorResponse> errorResponses, Throwable cause) {
        super(cause);
        this.httpStatus = httpStatus;
        this.errorResponses = errorResponses;
    }
}
