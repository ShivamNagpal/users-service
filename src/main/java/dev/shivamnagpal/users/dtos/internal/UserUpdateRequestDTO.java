package dev.shivamnagpal.users.dtos.internal;

import dev.shivamnagpal.users.utils.RequestConstants;
import dev.shivamnagpal.users.utils.RequestValidationUtils;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequestDTO {
    private String firstName;
    private String lastName;

    public static Future<UserUpdateRequestDTO> fromRequest(JsonObject body) {
        HashMap<String, String> errors = new HashMap<>();

        RequestValidationUtils.validateNotBlank(body, RequestConstants.FIRST_NAME, errors);
        RequestValidationUtils.validateNotBlank(body, RequestConstants.LAST_NAME, errors);

        if (!errors.isEmpty()) {
            return RequestValidationUtils.formErrorResponse(errors);
        }

        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO();
        userUpdateRequestDTO.setFirstName(body.getString(RequestConstants.FIRST_NAME));
        userUpdateRequestDTO.setLastName(body.getString(RequestConstants.LAST_NAME));
        return Future.succeededFuture(userUpdateRequestDTO);
    }
}
