package com.nagpal.shivam.workout_manager_user.exceptions;

import io.vertx.core.json.JsonObject;
import lombok.Getter;

@Getter
public class ResponseException extends AppException {
    private final int status;
    private final String message;
    private final JsonObject payload;

    public ResponseException(int status, String message, JsonObject payload) {
        super(message);
        this.status = status;
        this.message = message;
        this.payload = payload;
    }
}
