package dev.shivamnagpal.users.dtos.response.wrapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.shivamnagpal.users.enums.ResponseStatus;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseWrapper<T>(
        T data, PaginationResponse pagination, List<ErrorResponse> errors,
        MetaResponse metaResponse
) {

    public static <T> ResponseWrapper<T> success(T data) {
        return success(data, null);
    }

    public static <T> ResponseWrapper<T> success(T data, PaginationResponse pagination) {
        MetaResponse metaResponse = MetaResponse.builder()
                .status(ResponseStatus.SUCCESS)
                .timestamp(Instant.now().toEpochMilli())
                .build();
        return new ResponseWrapper<>(data, pagination, null, metaResponse);
    }

    public static <T> ResponseWrapper<T> failure(List<ErrorResponse> errorResponses) {
        return failure(errorResponses, null);
    }

    public static <T> ResponseWrapper<T> failure(List<ErrorResponse> errorResponses, T data) {
        MetaResponse metaResponse = MetaResponse.builder()
                .status(ResponseStatus.FAILURE)
                .timestamp(Instant.now().toEpochMilli())
                .build();
        return new ResponseWrapper<>(data, null, errorResponses, metaResponse);
    }

}
