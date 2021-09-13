package com.nagpal.shivam.workout_manager_user.services;

import com.nagpal.shivam.workout_manager_user.dtos.internal.JWTOTPTokenDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.VerifyOTPRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.OTPResponseDTO;
import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

public interface OTPService {
    Future<OTPResponseDTO> resendOTP(JWTOTPTokenDTO jwtotpTokenDTO);

    Future<Object> verifyOTP(JWTOTPTokenDTO jwtotpTokenDTO, VerifyOTPRequestDTO verifyOTPRequestDTO);

    Future<String> triggerEmailVerification(SqlClient sqlClient, Long userId, String email, OTPPurpose otpPurpose);
}
