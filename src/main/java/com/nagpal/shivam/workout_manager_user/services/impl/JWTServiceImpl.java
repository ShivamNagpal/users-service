package com.nagpal.shivam.workout_manager_user.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nagpal.shivam.workout_manager_user.dtos.internal.JWTOTPTokenDTO;
import com.nagpal.shivam.workout_manager_user.enums.Configuration;
import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import com.nagpal.shivam.workout_manager_user.utils.UtilMethods;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.Date;

public class JWTServiceImpl implements com.nagpal.shivam.workout_manager_user.services.JWTService {

    private final Algorithm otpSigningAlgorithm;
    private final JWTVerifier otpTokenVerifier;

    public JWTServiceImpl(JsonObject config) {
        otpSigningAlgorithm = Algorithm.HMAC512(config.getString(Configuration.OTP_SECRET_TOKEN.getKey()));
        otpTokenVerifier = JWT.require(otpSigningAlgorithm).withIssuer(Constants.ISSUER_WORKOUT_MANAGER).build();
    }

    @Override
    public String generateOTPToken(JWTOTPTokenDTO jwtotpTokenDTO) {
        return JWT.create()
                .withClaim(Constants.USER_ID, jwtotpTokenDTO.getUserId())
                .withClaim(Constants.EMAIL, jwtotpTokenDTO.getEmail())
                .withClaim(Constants.OTP_PURPOSE, jwtotpTokenDTO.getOtpPurpose().toString())
                .withExpiresAt(UtilMethods.convertLocalDateTimeToDate(
                        LocalDateTime.now().plusMinutes(Constants.OTP_EXPIRY_TIME)))
                .withIssuer(Constants.ISSUER_WORKOUT_MANAGER)
                .withIssuedAt(new Date())
                .sign(otpSigningAlgorithm);
    }

    @Override
    public Future<JWTOTPTokenDTO> verifyOTPToken(String otpToken) {
        DecodedJWT decodedJWT = null;
        try {
            decodedJWT = otpTokenVerifier.verify(otpToken);
        } catch (JWTVerificationException e) {
            return Future.failedFuture(
                    new ResponseException(HttpResponseStatus.BAD_REQUEST.code(), MessageConstants.INVALID_OTP_TOKEN,
                            null
                    )
            );
        }
        JWTOTPTokenDTO jwtotpTokenDTO = new JWTOTPTokenDTO();
        jwtotpTokenDTO.setUserId(decodedJWT.getClaim(Constants.USER_ID).asLong());
        jwtotpTokenDTO.setEmail(decodedJWT.getClaim(Constants.EMAIL).asString());
        jwtotpTokenDTO.setOtpPurpose(OTPPurpose.valueOf(decodedJWT.getClaim(Constants.OTP_PURPOSE).asString()));
        return Future.succeededFuture(jwtotpTokenDTO);
    }
}
