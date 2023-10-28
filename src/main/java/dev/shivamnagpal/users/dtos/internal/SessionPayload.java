package dev.shivamnagpal.users.dtos.internal;

import dev.shivamnagpal.users.exceptions.ResponseException;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionPayload {
    private String sessionId;
    private String refreshToken;

    public static Future<SessionPayload> fromRefreshToken(String refreshToken) {
        String[] split = refreshToken.split(Constants.REFRESH_TOKEN_SEPARATOR, 2);
        if (split.length != 2) {
            return Future.failedFuture(new ResponseException(HttpResponseStatus.BAD_REQUEST.code(),
                    MessageConstants.INVALID_REFRESH_TOKEN, null
            ));
        }
        SessionPayload sessionPayload = new SessionPayload();
        sessionPayload.setSessionId(split[0]);
        sessionPayload.setRefreshToken(split[1]);
        return Future.succeededFuture(sessionPayload);
    }

    public String createRefreshToken() {
        return sessionId + Constants.REFRESH_TOKEN_SEPARATOR + refreshToken;
    }
}
