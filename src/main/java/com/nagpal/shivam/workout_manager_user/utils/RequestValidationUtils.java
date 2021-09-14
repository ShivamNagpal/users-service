package com.nagpal.shivam.workout_manager_user.utils;

import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;
import java.util.Objects;

public class RequestValidationUtils {

    private RequestValidationUtils() {
    }

    public static Future<JsonObject> fetchBodyAsJson(RoutingContext routingContext) {
        JsonObject bodyAsJson = routingContext.getBodyAsJson();
        if (Objects.isNull(bodyAsJson)) {
            return Future.failedFuture(new ResponseException(HttpResponseStatus.BAD_REQUEST.code(),
                    MessageConstants.REQUEST_BODY_NOT_PROVIDED, null));
        }
        return Future.succeededFuture(bodyAsJson);
    }

    public static void validateNotNull(JsonObject requestBody, String key, Map<String, String> errors) {
        Object value = requestBody.getValue(key);
        if (Objects.isNull(value)) {
            errors.put(key, MessageConstants.MUST_NOT_BE_A_NULL_VALUE);
        }
    }

    public static void validateNotBlank(JsonObject requestBody, String key, Map<String, String> errors) {
        String value = requestBody.getString(key);
        if (Objects.isNull(value) || value.isBlank()) {
            errors.put(key, MessageConstants.MUST_NOT_BE_BLANK);
        }
    }

    public static <T> Future<T> formErrorResponse(Map<String, String> errors) {
        return Future.failedFuture(new ResponseException(HttpResponseStatus.BAD_REQUEST.code(),
                MessageConstants.VALIDATION_ERRORS_IN_THE_REQUEST, JsonObject.mapFrom(errors)));
    }
}
