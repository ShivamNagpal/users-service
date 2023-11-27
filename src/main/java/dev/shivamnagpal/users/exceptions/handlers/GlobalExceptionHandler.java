package dev.shivamnagpal.users.exceptions.handlers;

import dev.shivamnagpal.users.dtos.response.ResponseWrapper;
import dev.shivamnagpal.users.exceptions.ResponseException;
import dev.shivamnagpal.users.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalExceptionHandler {
    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    private GlobalExceptionHandler() {
    }

    public static void handle(Throwable throwable, HttpServerResponse httpServerResponse) {
        logger.log(Level.SEVERE, throwable.getMessage(), throwable);
        if (throwable instanceof ResponseException responseException) {
            Object responseExceptionPayload = responseException.getPayload();
            if (Objects.nonNull(responseExceptionPayload)) {
                String message = MessageFormat.format(
                        MessageConstants.RESPONSE_EXCEPTION_PAYLOAD,
                        Json.encodePrettily(responseExceptionPayload)
                );
                logger.log(Level.SEVERE, message);
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
