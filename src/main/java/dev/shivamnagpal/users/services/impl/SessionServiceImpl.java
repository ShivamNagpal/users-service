package dev.shivamnagpal.users.services.impl;

import dev.shivamnagpal.users.daos.RoleDao;
import dev.shivamnagpal.users.daos.SessionDao;
import dev.shivamnagpal.users.daos.UserDao;
import dev.shivamnagpal.users.dtos.internal.JWTAuthTokenDTO;
import dev.shivamnagpal.users.dtos.internal.SessionPayload;
import dev.shivamnagpal.users.dtos.request.RefreshSessionRequestDTO;
import dev.shivamnagpal.users.dtos.response.LoginResponseDTO;
import dev.shivamnagpal.users.enums.AccountStatus;
import dev.shivamnagpal.users.enums.SessionStatus;
import dev.shivamnagpal.users.exceptions.ResponseException;
import dev.shivamnagpal.users.models.Session;
import dev.shivamnagpal.users.models.User;
import dev.shivamnagpal.users.services.JWTService;
import dev.shivamnagpal.users.services.SessionService;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.pgclient.PgPool;

public class SessionServiceImpl implements SessionService {
    private final PgPool pgPool;
    private final MongoClient mongoClient;
    private final JsonObject config;
    private final SessionDao sessionDao;
    private final JWTService jwtService;
    private final UserDao userDao;
    private final RoleDao roleDao;

    public SessionServiceImpl(PgPool pgPool, MongoClient mongoClient, JsonObject config, SessionDao sessionDao,
                              JWTService jwtService, UserDao userDao, RoleDao roleDao) {
        this.pgPool = pgPool;
        this.mongoClient = mongoClient;
        this.config = config;
        this.sessionDao = sessionDao;
        this.jwtService = jwtService;
        this.userDao = userDao;
        this.roleDao = roleDao;
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
                .map(sessionPayload -> getLoginResponseDTO(userId, roles, sessionPayload));
    }

    @Override
    public Future<LoginResponseDTO> refreshSession(RefreshSessionRequestDTO refreshSessionRequestDTO) {
        return SessionPayload.fromRefreshToken(refreshSessionRequestDTO.getRefreshToken())
                .compose(sessionPayload ->
                        sessionDao.findById(mongoClient, sessionPayload.getSessionId())
                                .compose(sessionOptional -> {
                                    if (sessionOptional.isEmpty()) {
                                        return Future.failedFuture(
                                                new ResponseException(HttpResponseStatus.BAD_REQUEST.code(),
                                                        MessageConstants.INVALID_REFRESH_TOKEN, null)
                                        );
                                    }
                                    Session session = sessionOptional.get();
                                    if (session.getExpiryTime() < System.currentTimeMillis()) {
                                        return Future.failedFuture(
                                                new ResponseException(HttpResponseStatus.NOT_ACCEPTABLE.code(),
                                                        MessageConstants.SESSION_HAS_EXPIRED, null)
                                        );
                                    }
                                    if (session.getStatus() != SessionStatus.ACTIVE) {
                                        return Future.failedFuture(
                                                new ResponseException(HttpResponseStatus.NOT_ACCEPTABLE.code(),
                                                        MessageConstants.SESSION_IS_NOT_ACTIVE, null)
                                        );
                                    }
                                    return pgPool.withTransaction(
                                            sqlConnection -> userDao.getById(sqlConnection, session.getUserId())
                                                    .compose(userOptional -> {
                                                        if (userOptional.isEmpty()) {
                                                            return Future.failedFuture(
                                                                    new ResponseException(
                                                                            HttpResponseStatus.NOT_ACCEPTABLE.code(),
                                                                            MessageConstants.USER_NOT_FOUND, null)
                                                            );
                                                        }
                                                        User user = userOptional.get();
                                                        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                                                            return Future.failedFuture(
                                                                    new ResponseException(
                                                                            HttpResponseStatus.NOT_ACCEPTABLE.code(),
                                                                            MessageConstants.USER_ACCOUNT_IS_NOT_ACTIVE,
                                                                            null)
                                                            );
                                                        }
                                                        return Future.succeededFuture();
                                                    })
                                                    .compose(obj -> roleDao.fetchRolesByUserIdAndDeleted(sqlConnection,
                                                            session.getUserId(),
                                                            false
                                                    ))
                                                    .compose(roles -> {
                                                        if (!session.getCurrentRefreshToken()
                                                                .equals(sessionPayload.getRefreshToken())) {
                                                            Future<Void> future = Future.succeededFuture();
                                                            if (session.getUsedRefreshTokens()
                                                                    .contains(sessionPayload.getRefreshToken())) {
                                                                future =
                                                                        sessionDao.updateStatus(mongoClient,
                                                                                session.getId(), SessionStatus.VOID
                                                                        );
                                                            }
                                                            return future.compose(v -> Future.failedFuture(
                                                                    new ResponseException(
                                                                            HttpResponseStatus.BAD_REQUEST.code(),
                                                                            MessageConstants.INVALID_REFRESH_TOKEN, null
                                                                    )
                                                            ));
                                                        }
                                                        session.refresh();
                                                        sessionPayload.setRefreshToken(
                                                                session.getCurrentRefreshToken());
                                                        String[] roleNamesArray =
                                                                roles.stream().map(r -> r.getRoleName().name())
                                                                        .toArray(String[]::new);
                                                        return sessionDao.updateRefreshToken(mongoClient, session)
                                                                .map(getLoginResponseDTO(session.getUserId(),
                                                                        roleNamesArray, sessionPayload));
                                                    })
                                    );
                                })
                );
    }

    private LoginResponseDTO getLoginResponseDTO(Long userId, String[] roles, SessionPayload sessionPayload) {
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        JWTAuthTokenDTO jwtAuthTokenDTO = new JWTAuthTokenDTO();
        jwtAuthTokenDTO.setUserId(userId);
        jwtAuthTokenDTO.setSessionId(sessionPayload.getSessionId());
        jwtAuthTokenDTO.setRoles(roles);

        loginResponseDTO.setAuthToken(jwtService.generateAuthToken(jwtAuthTokenDTO));
        loginResponseDTO.setRefreshToken(sessionPayload.createRefreshToken());
        return loginResponseDTO;
    }
}
