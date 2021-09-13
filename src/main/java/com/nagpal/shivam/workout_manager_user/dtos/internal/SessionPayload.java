package com.nagpal.shivam.workout_manager_user.dtos.internal;

import com.nagpal.shivam.workout_manager_user.utils.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionPayload {
    private String sessionId;
    private String refreshToken;

    public static SessionPayload fromRefreshToken(String refreshToken) {
        String[] split = refreshToken.split(Constants.REFRESH_TOKEN_SEPARATOR, 2);
        SessionPayload sessionPayload = new SessionPayload();
        sessionPayload.setSessionId(split[0]);
        sessionPayload.setRefreshToken(split[1]);
        return sessionPayload;
    }

    public String createRefreshToken() {
        return sessionId + Constants.REFRESH_TOKEN_SEPARATOR + refreshToken;
    }
}
