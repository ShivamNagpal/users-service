package dev.shivamnagpal.users.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.shivamnagpal.users.dtos.internal.JWTAuthTokenDTO;
import dev.shivamnagpal.users.dtos.internal.JWTOTPTokenDTO;
import dev.shivamnagpal.users.enums.Configuration;
import dev.shivamnagpal.users.enums.OTPPurpose;
import dev.shivamnagpal.users.enums.RoleName;
import dev.shivamnagpal.users.exceptions.ResponseException;
import dev.shivamnagpal.users.services.JWTService;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.MessageConstants;
import dev.shivamnagpal.users.utils.UtilMethods;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Set;

public class JWTServiceImpl implements JWTService {

    private final JsonObject config;

    private final Algorithm otpSigningAlgorithm;

    private final Algorithm authTokenSigningAlgorithm;

    private final JWTVerifier otpTokenVerifier;

    private final JWTVerifier authTokenVerifier;

    public JWTServiceImpl(JsonObject config) {
        this.config = config;
        otpSigningAlgorithm = Algorithm.HMAC512(config.getString(Configuration.OTP_SECRET_TOKEN.getKey()));
        authTokenSigningAlgorithm = getRSAAlgorithm();
        String jwtIssuer = config.getString(Configuration.JWT_ISSUER.getKey());
        otpTokenVerifier = JWT.require(otpSigningAlgorithm).withIssuer(jwtIssuer).build();
        authTokenVerifier = JWT.require(authTokenSigningAlgorithm).withIssuer(jwtIssuer).build();
    }

    @SneakyThrows
    private Algorithm getRSAAlgorithm() {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] privateKeyBytes = decoder.decode(config.getString(Configuration.AUTH_TOKEN_PRIVATE_KEY.getKey()));
        byte[] publicKeyBytes = decoder.decode(config.getString(Configuration.AUTH_TOKEN_PUBLIC_KEY.getKey()));
        KeyFactory keyFactory = KeyFactory.getInstance(Constants.RSA);
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        return Algorithm.RSA512(publicKey, privateKey);
    }

    @Override
    public String generateOTPToken(JWTOTPTokenDTO jwtotpTokenDTO) {
        return JWT.create()
                .withClaim(Constants.USER_ID, jwtotpTokenDTO.getUserId())
                .withClaim(Constants.EMAIL, jwtotpTokenDTO.getEmail())
                .withClaim(Constants.OTP_PURPOSE, jwtotpTokenDTO.getOtpPurpose().toString())
                .withExpiresAt(
                        UtilMethods.convertLocalDateTimeToDate(
                                LocalDateTime.now().plusSeconds(config.getInteger(Constants.OTP_EXPIRY_TIME))
                        )
                )
                .withIssuer(Configuration.JWT_ISSUER.getKey())
                .withIssuedAt(new Date())
                .sign(otpSigningAlgorithm);
    }

    @Override
    public Future<JWTOTPTokenDTO> verifyAndDecodeOTPToken(String otpToken) {
        DecodedJWT decodedJWT;
        try {
            decodedJWT = otpTokenVerifier.verify(otpToken);
        } catch (JWTVerificationException e) {
            return Future.failedFuture(
                    new ResponseException(
                            HttpResponseStatus.BAD_REQUEST.code(), MessageConstants.INVALID_OTP_TOKEN,
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

    @Override
    public String generateAuthToken(JWTAuthTokenDTO jwtAuthTokenDTO) {
        return JWT.create()
                .withClaim(Constants.USER_ID, jwtAuthTokenDTO.getUserId())
                .withClaim(Constants.SESSION_ID, jwtAuthTokenDTO.getSessionId())
                .withArrayClaim(Constants.ROLES, jwtAuthTokenDTO.getRoles())
                .withExpiresAt(
                        UtilMethods.convertLocalDateTimeToDate(
                                LocalDateTime.now().plusSeconds(config.getInteger(Constants.JWT_EXPIRY_TIME))
                        )
                )
                .withIssuer(Configuration.JWT_ISSUER.getKey())
                .withIssuedAt(new Date())
                .sign(authTokenSigningAlgorithm);
    }

    @Override
    public Future<Void> verifyAuthToken(String authToken) {
        try {
            authTokenVerifier.verify(authToken);
            return Future.succeededFuture();
        } catch (JWTVerificationException e) {
            return Future.failedFuture(
                    new ResponseException(
                            HttpResponseStatus.BAD_REQUEST.code(), MessageConstants.INVALID_AUTH_TOKEN,
                            null
                    )
            );
        }
    }

    @Override
    public JWTAuthTokenDTO decodeAuthToken(String authToken) {
        DecodedJWT decodedJWT = JWT.decode(authToken);
        JWTAuthTokenDTO jwtAuthTokenDTO = new JWTAuthTokenDTO();
        jwtAuthTokenDTO.setUserId(decodedJWT.getClaim(Constants.USER_ID).asLong());
        jwtAuthTokenDTO.setSessionId(decodedJWT.getClaim(Constants.SESSION_ID).asString());
        jwtAuthTokenDTO.setRoles(decodedJWT.getClaim(Constants.ROLES).asArray(String.class));
        return jwtAuthTokenDTO;
    }

    @Override
    public Future<Void> verifyRoles(JWTAuthTokenDTO jwtAuthTokenDTO, RoleName... roles) {
        Set<String> userRoles = Set.of(jwtAuthTokenDTO.getRoles());
        for (RoleName roleName : roles) {
            if (userRoles.contains(roleName.name())) {
                return Future.succeededFuture();
            }
        }
        return Future.failedFuture(
                new ResponseException(
                        HttpResponseStatus.UNAUTHORIZED.code(),
                        MessageConstants.USER_IS_NOT_AUTHORIZED_TO_ACCESS, null
                )
        );
    }
}
