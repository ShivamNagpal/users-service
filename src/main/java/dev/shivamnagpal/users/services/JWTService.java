package dev.shivamnagpal.users.services;

import dev.shivamnagpal.users.dtos.internal.JWTAuthTokenDTO;
import dev.shivamnagpal.users.dtos.internal.JWTOTPTokenDTO;
import dev.shivamnagpal.users.enums.RoleName;
import io.vertx.core.Future;

public interface JWTService {
    String generateOTPToken(JWTOTPTokenDTO jwtotpTokenDTO);

    Future<JWTOTPTokenDTO> verifyAndDecodeOTPToken(String otpToken);

    String generateAuthToken(JWTAuthTokenDTO jwtAuthTokenDTO);

    Future<Void> verifyAuthToken(String authToken);

    JWTAuthTokenDTO decodeAuthToken(String authToken);

    Future<Void> verifyRoles(JWTAuthTokenDTO jwtAuthTokenDTO, RoleName... roles);
}
