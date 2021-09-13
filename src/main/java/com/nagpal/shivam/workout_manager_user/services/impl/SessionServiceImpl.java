package com.nagpal.shivam.workout_manager_user.services.impl;

import com.nagpal.shivam.workout_manager_user.dtos.internal.JWTAuthTokenDTO;
import com.nagpal.shivam.workout_manager_user.dtos.internal.SessionPayload;
import com.nagpal.shivam.workout_manager_user.dtos.response.LoginResponseDTO;
import com.nagpal.shivam.workout_manager_user.models.Session;
import com.nagpal.shivam.workout_manager_user.services.JWTService;
import com.nagpal.shivam.workout_manager_user.services.SessionService;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class SessionServiceImpl implements SessionService {
    private final JsonObject config;
    private final JWTService jwtService;

    public SessionServiceImpl(JsonObject config, JWTService jwtService) {
        this.config = config;
        this.jwtService = jwtService;
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

    @Override
    public Future<LoginResponseDTO> createNewSessionAndFormLoginResponse(MongoClient mongoClient, Long userId,
                                                                         String[] roles) {
        return createNewSession(mongoClient, userId)
                .map(sessionPayload -> {
                    LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
                    JWTAuthTokenDTO jwtAuthTokenDTO = new JWTAuthTokenDTO();
                    jwtAuthTokenDTO.setUserId(userId);
                    jwtAuthTokenDTO.setSessionId(sessionPayload.getSessionId());
                    jwtAuthTokenDTO.setRoles(roles);

                    loginResponseDTO.setAuthToken(jwtService.generateAuthToken(jwtAuthTokenDTO));
                    loginResponseDTO.setRefreshToken(sessionPayload.createRefreshToken());
                    return loginResponseDTO;
                });
    }
}
