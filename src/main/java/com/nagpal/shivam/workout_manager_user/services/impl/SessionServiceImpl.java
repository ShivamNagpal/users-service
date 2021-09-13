package com.nagpal.shivam.workout_manager_user.services.impl;

import com.nagpal.shivam.workout_manager_user.dtos.internal.SessionPayload;
import com.nagpal.shivam.workout_manager_user.models.Session;
import com.nagpal.shivam.workout_manager_user.services.SessionService;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class SessionServiceImpl implements SessionService {
    private final JsonObject config;


    public SessionServiceImpl(JsonObject config) {
        this.config = config;
    }

    @Override
    public Future<SessionPayload> createNewSession(MongoClient mongoClient, Long userId) {
        Session session = Session.newDocument(userId, config);
        return mongoClient.save(Constants.SESSION, JsonObject.mapFrom(session))
                .map(sessionId -> {
                    SessionPayload sessionPayload = new SessionPayload();
                    sessionPayload.setSessionId(sessionId);
                    sessionPayload.setRefreshToken(session.getCurrentRefreshToken());
                    return sessionPayload;
                });
    }
}
