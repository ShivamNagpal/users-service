package dev.shivamnagpal.users.dtos.request;

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
public class LoginRequestDTO {
    private String email;

    private String password;

    public static Future<LoginRequestDTO> fromRequest(JsonObject body) {
        HashMap<String, String> errors = new HashMap<>();
        RequestValidationUtils.validateNotBlank(body, RequestConstants.EMAIL, errors);
        RequestValidationUtils.validateNotBlank(body, RequestConstants.PASSWORD, errors);

        if (!errors.isEmpty()) {
            return RequestValidationUtils.formErrorResponse(errors);
        }

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail(body.getString(RequestConstants.EMAIL));
        loginRequestDTO.setPassword(body.getString(RequestConstants.PASSWORD));
        return Future.succeededFuture(loginRequestDTO);
    }
}
