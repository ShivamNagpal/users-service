package com.nagpal.shivam.workout_manager_user.dtos.request;

import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import com.nagpal.shivam.workout_manager_user.utils.RequestConstants;
import com.nagpal.shivam.workout_manager_user.utils.RequestValidationUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
public class VerifyOTPRequestDTO {
    private Integer otp;

    public static Future<VerifyOTPRequestDTO> fromRequest(JsonObject body) {
        VerifyOTPRequestDTO verifyOTPRequestDTO = new VerifyOTPRequestDTO();
        HashMap<String, String> errors = new HashMap<>();

        RequestValidationUtils.validateNotNull(body, RequestConstants.OTP, errors);

        if (!errors.isEmpty()) {
            return Future.failedFuture(new ResponseException(HttpResponseStatus.BAD_REQUEST.code(),
                    MessageConstants.VALIDATION_ERRORS_IN_THE_REQUEST, JsonObject.mapFrom(errors)));
        }

        verifyOTPRequestDTO.setOtp(body.getInteger(RequestConstants.OTP));

        return Future.succeededFuture(verifyOTPRequestDTO);
    }
}
