package com.nagpal.shivam.workout_manager_user.services;

import com.nagpal.shivam.workout_manager_user.dtos.internal.JWTOTPTokenDTO;
import io.vertx.core.Future;

public interface JWTService {
    String generateOTPToken(JWTOTPTokenDTO jwtotpTokenDTO);

    Future<JWTOTPTokenDTO> verifyOTPToken(String otpToken);
}
