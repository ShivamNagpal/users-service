package dev.shivamnagpal.users.daos.impl;

import dev.shivamnagpal.users.daos.SessionDao;
import dev.shivamnagpal.users.enums.SessionStatus;
import dev.shivamnagpal.users.models.Session;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.DocumentConstants;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

import java.util.ArrayList;
import java.util.Optional;

public class SessionDaoImpl implements SessionDao {

    @Override
    public Future<Optional<Session>> findById(MongoClient mongoClient, String id) {
        return mongoClient.findOne(Constants.SESSION, new JsonObject().put(DocumentConstants.ID, id), null)
                .map(jsonObject -> {
                    if (jsonObject == null) {
                        return Optional.empty();
                    }
                    return Optional.of(Session.fromJsonObject(jsonObject));
                });
    }

    @Override
    public Future<Void> updateStatus(MongoClient mongoClient, String id, SessionStatus status) {
        return mongoClient.updateCollection(Constants.SESSION, new JsonObject().put(DocumentConstants.ID, id),
                        new JsonObject().put(Constants.DOLLAR_SET, new JsonObject()
                                .put(DocumentConstants.STATUS, status)
                                .put(DocumentConstants.TIME_LAST_MODIFIED, System.currentTimeMillis())
                        )
                )
                .compose(mongoClientUpdateResult -> Future.succeededFuture());
    }

    @Override
    public Future<Void> logoutSession(MongoClient mongoClient, String id) {
        return mongoClient.updateCollection(Constants.SESSION,
                        new JsonObject().put(DocumentConstants.ID, id)
                                .put(DocumentConstants.STATUS, SessionStatus.ACTIVE),
                        new JsonObject().put(Constants.DOLLAR_SET, new JsonObject()
                                .put(DocumentConstants.STATUS, SessionStatus.CLOSED)
                                .put(DocumentConstants.TIME_LAST_MODIFIED, System.currentTimeMillis())
                        )
                )
                .compose(mongoClientUpdateResult -> Future.succeededFuture());
    }

    @Override
    public Future<Void> logoutAllSessions(MongoClient mongoClient, Long userId) {
        return mongoClient.updateCollectionWithOptions(Constants.SESSION,
                        new JsonObject().put(DocumentConstants.USER_ID, userId)
                                .put(DocumentConstants.STATUS, SessionStatus.ACTIVE),
                        new JsonObject().put(Constants.DOLLAR_SET, new JsonObject()
                                .put(DocumentConstants.STATUS, SessionStatus.CLOSED)
                                .put(DocumentConstants.TIME_LAST_MODIFIED, System.currentTimeMillis())
                        ),
                        new UpdateOptions().setMulti(true)
                )
                .compose(mongoClientUpdateResult -> Future.succeededFuture());
    }

    @Override
    public Future<Void> updateRefreshToken(MongoClient mongoClient, Session session) {
        return mongoClient.updateCollection(Constants.SESSION,
                        new JsonObject().put(DocumentConstants.ID, session.getId()),
                        new JsonObject().put(Constants.DOLLAR_SET, new JsonObject()
                                .put(DocumentConstants.CURRENT_REFRESH_TOKEN, session.getCurrentRefreshToken())
                                .put(DocumentConstants.USED_REFRESH_TOKENS,
                                        new JsonArray(new ArrayList<>(session.getUsedRefreshTokens()))
                                )
                                .put(DocumentConstants.TIME_LAST_MODIFIED, System.currentTimeMillis())
                        )
                )
                .compose(mongoClientUpdateResult -> Future.succeededFuture());
    }
}
