package dev.shivamnagpal.users.models;

import dev.shivamnagpal.users.enums.SessionStatus;
import dev.shivamnagpal.users.utils.Constants;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Session extends BaseDocument {
    private Long userId;

    private String jwtFingerprint;

    private String currentRefreshToken;

    private Set<String> usedRefreshTokens;

    private SessionStatus status;

    private Long expiryTime;

    public static Session fromJsonObject(JsonObject jsonObject) {
        return jsonObject.mapTo(Session.class);
    }

    public static Session newDocument(Long userId, JsonObject config) {
        Session session = new Session();
        long currentTimeMillis = System.currentTimeMillis();
        session.setTimeCreated(currentTimeMillis);
        session.setTimeLastModified(currentTimeMillis);
        session.setUserId(userId);
        session.setCurrentRefreshToken(UUID.randomUUID().toString());
        session.setUsedRefreshTokens(Set.of());
        session.setStatus(SessionStatus.ACTIVE);
        session.setExpiryTime(currentTimeMillis + config.getInteger(Constants.SESSION_EXPIRY_TIME) * 1000);
        return session;
    }

    public void refresh() {
        this.getUsedRefreshTokens().add(this.getCurrentRefreshToken());
        this.setCurrentRefreshToken(UUID.randomUUID().toString());
    }
}
