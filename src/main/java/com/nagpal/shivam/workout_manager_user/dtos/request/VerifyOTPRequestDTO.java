package com.nagpal.shivam.workout_manager_user.dtos.request;

import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
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
public class VerifyOTPRequestDTO extends PasswordUpdateRequestDTO {
    private Integer otp;

    public static Future<VerifyOTPRequestDTO> fromRequest(JsonObject body, JsonObject config, OTPPurpose otpPurpose) {
        VerifyOTPRequestDTO verifyOTPRequestDTO = new VerifyOTPRequestDTO();
        HashMap<String, String> errors = new HashMap<>();

        RequestValidationUtils.validateNotNull(body, RequestConstants.OTP, errors);
        if (otpPurpose == OTPPurpose.RESET_PASSWORD) {
            RequestValidationUtils.validateNotBlank(body, RequestConstants.PASSWORD, errors);
        }

        if (!errors.isEmpty()) {
            return RequestValidationUtils.formErrorResponse(errors);
        }

        verifyOTPRequestDTO.setOtp(body.getInteger(RequestConstants.OTP));
        if (otpPurpose == OTPPurpose.RESET_PASSWORD) {
            String password = body.getString(RequestConstants.PASSWORD);
            verifyOTPRequestDTO.setPlainPassword(password);
            verifyOTPRequestDTO.setHashedPassword(BCrypt.hashpw(password,
                    BCrypt.gensalt(config.getInteger(Constants.BCRYPT_PASSWORD_LOG_ROUNDS))
            ));
        }

        return Future.succeededFuture(verifyOTPRequestDTO);
    }
}
