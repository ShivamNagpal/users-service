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
public class RoleUpdateRequestDTO {
    private Long userId;

    public static Future<RoleUpdateRequestDTO> fromRequest(JsonObject body) {
        HashMap<String, String> errors = new HashMap<>();
        RequestValidationUtils.validateNotNull(body, RequestConstants.USER_ID, errors);

        if (!errors.isEmpty()) {
            return RequestValidationUtils.formErrorResponse(errors);
        }

        RoleUpdateRequestDTO roleUpdateRequestDTO = new RoleUpdateRequestDTO();
        roleUpdateRequestDTO.setUserId(body.getLong(RequestConstants.USER_ID));
        return Future.succeededFuture(roleUpdateRequestDTO);
    }
}
