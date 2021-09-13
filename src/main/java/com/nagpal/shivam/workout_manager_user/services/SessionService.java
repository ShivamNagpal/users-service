package com.nagpal.shivam.workout_manager_user.services;

import com.nagpal.shivam.workout_manager_user.dtos.internal.SessionPayload;
import com.nagpal.shivam.workout_manager_user.dtos.response.LoginResponseDTO;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;

public interface SessionService {
    Future<SessionPayload> createNewSession(MongoClient mongoClient, Long userId);

    Future<LoginResponseDTO> createNewSessionAndFormLoginResponse(MongoClient mongoClient, Long userId, String[] roles);
}
