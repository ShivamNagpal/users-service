package com.nagpal.shivam.workout_manager_user.dtos.request;

import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.RequestConstants;
import com.nagpal.shivam.workout_manager_user.utils.RequestValidationUtils;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
public class PasswordUpdateRequestDTO {
    private String plainPassword;
    private String hashedPassword;

    public static Future<PasswordUpdateRequestDTO> fromRequest(JsonObject body, JsonObject config) {
        HashMap<String, String> errors = new HashMap<>();

        RequestValidationUtils.validateNotBlank(body, RequestConstants.PASSWORD, errors);

        if (!errors.isEmpty()) {
            return RequestValidationUtils.formErrorResponse(errors);
        }

        PasswordUpdateRequestDTO passwordUpdateRequestDTO = new PasswordUpdateRequestDTO();
        String password = body.getString(RequestConstants.PASSWORD);
        passwordUpdateRequestDTO.setPlainPassword(password);
        passwordUpdateRequestDTO.setHashedPassword(BCrypt.hashpw(password,
                BCrypt.gensalt(config.getInteger(Constants.BCRYPT_PASSWORD_LOG_ROUNDS))
        ));

        return Future.succeededFuture(passwordUpdateRequestDTO);
    }
}
