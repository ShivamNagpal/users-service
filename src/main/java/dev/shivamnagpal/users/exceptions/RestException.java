package dev.shivamnagpal.users.exceptions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.shivamnagpal.users.dtos.response.wrapper.ErrorResponse;
import dev.shivamnagpal.users.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class RestException extends AppException {

    private final int httpStatusCode;

    private final List<ErrorResponse> errorResponses;

    @Setter
    private ObjectNode payload;

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
        super(MessageConstants.REST_EXCEPTION_THROWN, cause);
        this.httpStatusCode = httpStatus.code();
        this.errorResponses = errorResponses;
    }
}
