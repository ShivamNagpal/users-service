package com.nagpal.shivam.workout_manager_user.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.nagpal.shivam.workout_manager_user.enums.Configuration;
import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.UtilMethods;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;

public class JWTServiceImpl implements com.nagpal.shivam.workout_manager_user.services.JWTService {

    private final Algorithm otpSigningAlgorithm;

    public JWTServiceImpl(JsonObject config) {
        otpSigningAlgorithm = Algorithm.HMAC512(config.getString(Configuration.OTP_SECRET_TOKEN.getKey()));
    }

    @Override
    public String generateOTPToken(Long userId, String email, OTPPurpose otpPurpose) {
        return JWT.create()
                .withClaim(Constants.USER_ID, userId)
                .withClaim(Constants.EMAIL, email)
                .withClaim(Constants.PURPOSE, otpPurpose.toString())
                .withExpiresAt(UtilMethods.convertLocalDateTimeToDate(
                        LocalDateTime.now().plusMinutes(Constants.OTP_EXPIRY_TIME)))
                .sign(otpSigningAlgorithm);
    }
}
