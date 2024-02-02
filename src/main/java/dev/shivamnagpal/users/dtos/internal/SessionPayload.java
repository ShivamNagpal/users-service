package dev.shivamnagpal.users.dtos.internal;

import dev.shivamnagpal.users.dtos.response.wrapper.ErrorResponse;
import dev.shivamnagpal.users.enums.ErrorCode;
import dev.shivamnagpal.users.exceptions.RestException;
import dev.shivamnagpal.users.utils.Constants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import lombok.Builder;

@Builder
public record SessionPayload(String sessionId, String refreshToken) {

    public static Future<SessionPayload> fromRefreshToken(String refreshToken) {
        String[] split = refreshToken.split(Constants.REFRESH_TOKEN_SEPARATOR, 2);
        if (split.length != 2) {
            return Future.failedFuture(
                    new RestException(
                            HttpResponseStatus.BAD_REQUEST,
                            ErrorResponse.from(ErrorCode.INVALID_REFRESH_TOKEN)
                    )
            );
        }
        SessionPayload sessionPayload = SessionPayload.builder()
                .sessionId(split[0])
                .refreshToken(split[1])
                .build();
        return Future.succeededFuture(sessionPayload);
    }

    public String createRefreshToken() {
        return sessionId + Constants.REFRESH_TOKEN_SEPARATOR + refreshToken;
    }
}
