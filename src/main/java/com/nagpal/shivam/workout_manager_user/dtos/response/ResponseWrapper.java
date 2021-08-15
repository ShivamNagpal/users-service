package com.nagpal.shivam.workout_manager_user.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ResponseWrapper<T> {
  private boolean success;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T payload;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String message;

  public static <T> ResponseWrapper<T> success(T payload, String message) {
    return new ResponseWrapper<>(true, payload, message);
  }

  public static <T> ResponseWrapper<T> success(T payload) {
    return success(payload, null);
  }

  public static <T> ResponseWrapper<T> failure(T payload, String message) {
    return new ResponseWrapper<>(false, payload, message);
  }

  public static <T> ResponseWrapper<T> failure(T payload) {
    return failure(payload, null);
  }
}
