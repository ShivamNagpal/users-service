package com.nagpal.shivam.workout_manager_user.models;

import com.nagpal.shivam.workout_manager_user.enums.SessionStatus;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Session extends BaseDocument {
    private Long userId;
    private String jwtFingerprint;
    private String currentRefreshToken;
    private List<String> usedRefreshTokens;
    private SessionStatus status;
    private Long expiryTime;

    public static Session fromJsonObject(JsonObject jsonObject) {
        return jsonObject.mapTo(Session.class);
    }
}
