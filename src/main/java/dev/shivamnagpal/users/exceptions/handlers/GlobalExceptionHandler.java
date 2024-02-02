package dev.shivamnagpal.users.exceptions.handlers;

import dev.shivamnagpal.users.dtos.response.ResponseWrapper;
import dev.shivamnagpal.users.exceptions.RestException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalExceptionHandler {

    private GlobalExceptionHandler() {
    }

    public static void handle(Throwable throwable, HttpServerResponse httpServerResponse) {
        log.error(throwable.getMessage(), throwable);
        if (throwable instanceof RestException restException) {
            ResponseWrapper<Object> failureResponseWrapper = ResponseWrapper
                    .failure(restException.getErrorResponses());
            httpServerResponse.setStatusCode(restException.getHttpStatus().code())
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
