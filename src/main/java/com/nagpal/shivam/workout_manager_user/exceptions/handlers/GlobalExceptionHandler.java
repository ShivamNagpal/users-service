package com.nagpal.shivam.workout_manager_user.exceptions.handlers;

import com.nagpal.shivam.workout_manager_user.dtos.response.ResponseWrapper;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;


public class GlobalExceptionHandler {

  public static void handle(Throwable throwable, HttpServerResponse httpServerResponse) {
    if (throwable instanceof ResponseException) {
      ResponseException responseException = (ResponseException) throwable;
      ResponseWrapper<Object> failureResponseWrapper =
        ResponseWrapper.failure(responseException.getPayload(), responseException.getMessage());
      httpServerResponse.setStatusCode(responseException.getStatus()).end(Json.encodePrettily(failureResponseWrapper));
    } else {
      HttpResponseStatus internalServerError = HttpResponseStatus.INTERNAL_SERVER_ERROR;
      ResponseWrapper<Object> failureResponseWrapper =
        ResponseWrapper.failure(null, internalServerError.reasonPhrase());
      httpServerResponse.setStatusCode(internalServerError.code()).end(Json.encodePrettily(failureResponseWrapper));
    }
  }
}
