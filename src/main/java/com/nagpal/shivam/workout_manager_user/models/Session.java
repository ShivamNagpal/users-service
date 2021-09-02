package com.nagpal.shivam.workout_manager_user.models;

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
    private String status;
    private Long expiryTime;

    public static Session fromJsonObject(JsonObject jsonObject) {
        return jsonObject.mapTo(Session.class);
    }
}
