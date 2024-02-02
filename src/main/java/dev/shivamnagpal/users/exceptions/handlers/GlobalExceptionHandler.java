package dev.shivamnagpal.users.exceptions.handlers;

import dev.shivamnagpal.users.dtos.response.ResponseWrapper;
import dev.shivamnagpal.users.dtos.response.wrapper.ErrorResponse;
import dev.shivamnagpal.users.enums.ErrorCode;
import dev.shivamnagpal.users.exceptions.RestException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GlobalExceptionHandler {

    private GlobalExceptionHandler() {
    }

    public static void handle(Throwable throwable, HttpServerResponse httpServerResponse) {
        log.error(throwable.getMessage(), throwable);
        if (throwable instanceof RestException restException) {
            ResponseWrapper<Object> failureResponseWrapper = ResponseWrapper
                    .failure(restException.getErrorResponses(), restException.getPayload());
            httpServerResponse.setStatusCode(restException.getHttpStatusCode())
                    .end(Json.encodePrettily(failureResponseWrapper));
        } else {
            ResponseWrapper<Object> failureResponseWrapper = ResponseWrapper
                    .failure(List.of(ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR)));
            httpServerResponse.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                    .end(Json.encodePrettily(failureResponseWrapper));
        }
    }
}
