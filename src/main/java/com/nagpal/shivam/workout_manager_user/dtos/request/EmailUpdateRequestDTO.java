package com.nagpal.shivam.workout_manager_user.dtos.request;

import com.nagpal.shivam.workout_manager_user.utils.RequestConstants;
import com.nagpal.shivam.workout_manager_user.utils.RequestValidationUtils;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
public class EmailUpdateRequestDTO {
    private String email;

    public static Future<EmailUpdateRequestDTO> fromRequest(JsonObject body) {
        HashMap<String, String> errors = new HashMap<>();

        RequestValidationUtils.validateNotBlank(body, RequestConstants.EMAIL, errors);

        if (!errors.isEmpty()) {
            return RequestValidationUtils.formErrorResponse(errors);
        }

        EmailUpdateRequestDTO emailUpdateRequestDTO = new EmailUpdateRequestDTO();
        emailUpdateRequestDTO.setEmail(body.getString(RequestConstants.EMAIL));

        return Future.succeededFuture(emailUpdateRequestDTO);
    }
}
