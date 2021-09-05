package com.nagpal.shivam.workout_manager_user.exceptions.handlers;

import com.nagpal.shivam.workout_manager_user.dtos.ReplyExceptionMessage;
import com.nagpal.shivam.workout_manager_user.dtos.response.ResponseWrapper;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

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
        if (throwable instanceof ResponseException) {
            ResponseException responseException = (ResponseException) throwable;
            JsonObject responseExceptionPayload = responseException.getPayload();
            if (Objects.nonNull(responseExceptionPayload)) {
                String message = MessageFormat.format(MessageConstants.RESPONSE_EXCEPTION_PAYLOAD,
                        responseExceptionPayload.encodePrettily());
                logger.log(Level.SEVERE, message);
            }
            ResponseWrapper<Object> failureResponseWrapper =
                    ResponseWrapper.failure(responseExceptionPayload, responseException.getMessage());
            httpServerResponse.setStatusCode(responseException.getStatus())
                    .end(Json.encodePrettily(failureResponseWrapper));
        } else if (throwable instanceof ReplyException) {
            ReplyException replyException = (ReplyException) throwable;
            if (replyException.failureCode() == Constants.MESSAGE_FAILURE_UNHANDLED) {
                handleUnknownException(httpServerResponse);
                return;
            }
            ReplyExceptionMessage replyExceptionMessage =
                    Json.decodeValue(replyException.getMessage(), ReplyExceptionMessage.class);
            ResponseWrapper<Object> failureResponseWrapper =
                    ResponseWrapper.failure(replyExceptionMessage.getPayload(), replyExceptionMessage.getMessage());
            httpServerResponse.setStatusCode(replyExceptionMessage.getStatus())
                    .end(Json.encodePrettily(failureResponseWrapper));
        } else {
            handleUnknownException(httpServerResponse);
        }
    }

    private static void handleUnknownException(HttpServerResponse httpServerResponse) {
        HttpResponseStatus internalServerError = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        ResponseWrapper<Object> failureResponseWrapper =
                ResponseWrapper.failure(null, internalServerError.reasonPhrase());
        httpServerResponse.setStatusCode(internalServerError.code())
                .end(Json.encodePrettily(failureResponseWrapper));
    }
}
