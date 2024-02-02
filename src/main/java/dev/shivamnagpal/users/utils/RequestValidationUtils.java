package dev.shivamnagpal.users.utils;

import dev.shivamnagpal.users.dtos.response.wrapper.ErrorResponse;
import dev.shivamnagpal.users.enums.ErrorCode;
import dev.shivamnagpal.users.exceptions.RestException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RequestValidationUtils {

    private RequestValidationUtils() {
    }

    public static Future<JsonObject> fetchBodyAsJson(RoutingContext routingContext) {
        JsonObject bodyAsJson = routingContext.body().asJsonObject();
        if (Objects.isNull(bodyAsJson)) {
            return Future.failedFuture(
                    new RestException(
                            HttpResponseStatus.BAD_REQUEST,
                            ErrorResponse.from(ErrorCode.REQUEST_BODY_NOT_PROVIDED)
                    )
            );
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
        List<ErrorResponse> errorResponses = errors.entrySet()
                .stream()
                .map(
                        entry -> ErrorResponse.from(
                                ErrorCode.VALIDATION_ERRORS_IN_THE_REQUEST,
                                String.format(Constants.VALIDATION_ERROR_FORMAT, entry.getKey(), entry.getValue())

                        )
                )
                .toList();
        return Future.failedFuture(
                new RestException(HttpResponseStatus.BAD_REQUEST, errorResponses)
        );
    }

    public static Future<Boolean> getBooleanQueryParam(HttpServerRequest request, String key) {
        String paramVal = request.getParam(key);
        if (paramVal == null) {
            return Future.succeededFuture(null);
        }
        if (paramVal.equalsIgnoreCase(Constants.TRUE)) {
            return Future.succeededFuture(true);
        } else if (paramVal.equalsIgnoreCase(Constants.FALSE)) {
            return Future.succeededFuture(false);
        }
        return Future.failedFuture(
                new RestException(
                        HttpResponseStatus.BAD_REQUEST,
                        ErrorResponse.from(
                                ErrorCode.VALIDATION_ERRORS_IN_THE_REQUEST,
                                MessageFormat.format(MessageConstants.QUERY_PARAM_MUST_HAVE_ONLY_BOOLEAN_VALUES, key)
                        )
                )
        );
    }

    public static Future<Boolean> getBooleanQueryParam(HttpServerRequest request, String key, boolean defaultValue) {
        return getBooleanQueryParam(request, key)
                .map(value -> Objects.requireNonNullElse(value, defaultValue));
    }
}
