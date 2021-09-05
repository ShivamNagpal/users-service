package com.nagpal.shivam.workout_manager_user.dtos;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReplyExceptionMessage {
    private int status;
    private String message;
    private JsonObject payload;
}
