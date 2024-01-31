package dev.shivamnagpal.users.exceptions.handlers;

import dev.shivamnagpal.users.dtos.response.ResponseWrapper;
import dev.shivamnagpal.users.exceptions.ResponseException;
import dev.shivamnagpal.users.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Objects;

@Slf4j
public class GlobalExceptionHandler {

    private GlobalExceptionHandler() {
    }

    public static void handle(Throwable throwable, HttpServerResponse httpServerResponse) {
        log.error(throwable.getMessage(), throwable);
        if (throwable instanceof ResponseException responseException) {
            Object responseExceptionPayload = responseException.getPayload();
            if (Objects.nonNull(responseExceptionPayload)) {
                String message = MessageFormat.format(
                        MessageConstants.RESPONSE_EXCEPTION_PAYLOAD,
                        Json.encodePrettily(responseExceptionPayload)
                );
                log.error(message);
            }
            ResponseWrapper<Object> failureResponseWrapper = ResponseWrapper
                    .failure(responseExceptionPayload, responseException.getMessage());
            httpServerResponse.setStatusCode(responseException.getStatus())
                    .end(Json.encodePrettily(failureResponseWrapper));
        } else {
            HttpResponseStatus internalServerError = HttpResponseStatus.INTERNAL_SERVER_ERROR;
            ResponseWrapper<Object> failureResponseWrapper = ResponseWrapper
                    .failure(null, internalServerError.reasonPhrase());
            httpServerResponse.setStatusCode(internalServerError.code())
                    .end(Json.encodePrettily(failureResponseWrapper));
        }
    }
}
