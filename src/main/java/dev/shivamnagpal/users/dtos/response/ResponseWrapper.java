package dev.shivamnagpal.users.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.shivamnagpal.users.dtos.response.wrapper.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ResponseWrapper<T> {
    private List<ErrorResponse> errors;

    private boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T payload;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    public static <T> ResponseWrapper<T> success(T payload, String message) {
        return new ResponseWrapper<>(null, true, payload, message);
    }

    public static <T> ResponseWrapper<T> success(T payload) {
        return success(payload, null);
    }

    @Deprecated
    public static <T> ResponseWrapper<T> failure(T payload, String message) {
        return new ResponseWrapper<>(null, false, payload, message);
    }

    public static <T> ResponseWrapper<T> failure(List<ErrorResponse> errors) {
        return new ResponseWrapper<>(errors, false, null, null);
    }

}
