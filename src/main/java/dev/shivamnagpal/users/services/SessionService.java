package dev.shivamnagpal.users.services;

import dev.shivamnagpal.users.dtos.internal.SessionPayload;
import dev.shivamnagpal.users.dtos.request.RefreshSessionRequestDTO;
import dev.shivamnagpal.users.dtos.response.LoginResponseDTO;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;

public interface SessionService {
    Future<SessionPayload> createNewSession(MongoClient mongoClient, Long userId);

    Future<LoginResponseDTO> createNewSessionAndFormLoginResponse(MongoClient mongoClient, Long userId, String[] roles);

    Future<LoginResponseDTO> refreshSession(RefreshSessionRequestDTO refreshSessionRequestDTO);
}
