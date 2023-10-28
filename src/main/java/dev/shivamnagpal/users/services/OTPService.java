package dev.shivamnagpal.users.services;

import dev.shivamnagpal.users.dtos.internal.JWTOTPTokenDTO;
import dev.shivamnagpal.users.dtos.request.VerifyOTPRequestDTO;
import dev.shivamnagpal.users.dtos.response.OTPResponseDTO;
import dev.shivamnagpal.users.enums.OTPPurpose;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

public interface OTPService {
    Future<OTPResponseDTO> resendOTP(JWTOTPTokenDTO jwtotpTokenDTO);

    Future<Object> verifyOTP(JWTOTPTokenDTO jwtotpTokenDTO, VerifyOTPRequestDTO verifyOTPRequestDTO);

    Future<String> triggerEmailVerification(SqlClient sqlClient, Long userId, String email, OTPPurpose otpPurpose);
}
