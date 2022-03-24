package com.nagpal.shivam.workout_manager_user.daos;

import com.nagpal.shivam.workout_manager_user.enums.SessionStatus;
import com.nagpal.shivam.workout_manager_user.models.Session;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;

import java.util.Optional;

public interface SessionDao {
    Future<Optional<Session>> findById(MongoClient mongoClient, String id);

    Future<Void> updateStatus(MongoClient mongoClient, String id, SessionStatus status);

    Future<Void> logoutSession(MongoClient mongoClient, String id);

    Future<Void> logoutAllSessions(MongoClient mongoClient, Long userId);

    Future<Void> updateRefreshToken(MongoClient mongoClient, Session session);
}
