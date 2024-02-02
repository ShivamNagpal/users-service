package dev.shivamnagpal.users.services.impl;

import dev.shivamnagpal.users.daos.RoleDao;
import dev.shivamnagpal.users.daos.SessionDao;
import dev.shivamnagpal.users.daos.UserDao;
import dev.shivamnagpal.users.dtos.internal.JWTAuthTokenDTO;
import dev.shivamnagpal.users.dtos.internal.SessionPayload;
import dev.shivamnagpal.users.dtos.request.RefreshSessionRequestDTO;
import dev.shivamnagpal.users.dtos.response.LoginResponseDTO;
import dev.shivamnagpal.users.dtos.response.wrapper.ErrorResponse;
import dev.shivamnagpal.users.enums.AccountStatus;
import dev.shivamnagpal.users.enums.ErrorCode;
import dev.shivamnagpal.users.enums.SessionStatus;
import dev.shivamnagpal.users.exceptions.RestException;
import dev.shivamnagpal.users.models.Session;
import dev.shivamnagpal.users.services.JWTService;
import dev.shivamnagpal.users.services.SessionService;
import dev.shivamnagpal.users.utils.Constants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.sqlclient.Pool;

public class SessionServiceImpl implements SessionService {
    private final Pool pgPool;

    private final MongoClient mongoClient;

    private final JsonObject config;

    private final SessionDao sessionDao;

    private final JWTService jwtService;

    private final UserDao userDao;

    private final RoleDao roleDao;

    public SessionServiceImpl(
            Pool pgPool,
            MongoClient mongoClient,
            JsonObject config,
            SessionDao sessionDao,
            JWTService jwtService,
            UserDao userDao,
            RoleDao roleDao
    ) {
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
                .map(
                        sessionId -> SessionPayload.builder()
                                .sessionId(sessionId)
                                .refreshToken(session.getCurrentRefreshToken())
                                .build()
                );
    }

    @Override
    public Future<LoginResponseDTO> createNewSessionAndFormLoginResponse(
            MongoClient mongoClient,
            Long userId,
            String[] roles
    ) {
        return createNewSession(mongoClient, userId)
                .map(sessionPayload -> getLoginResponseDTO(userId, roles, sessionPayload));
    }

    @Override
    public Future<LoginResponseDTO> refreshSession(RefreshSessionRequestDTO refreshSessionRequestDTO) {
        return SessionPayload.fromRefreshToken(refreshSessionRequestDTO.getRefreshToken())
                .compose(
                        sessionPayload -> sessionDao.findById(mongoClient, sessionPayload.sessionId())
                                .compose(
                                        sessionOptional -> sessionOptional.map(Future::succeededFuture)
                                                .orElseGet(
                                                        () -> Future.failedFuture(
                                                                new RestException(
                                                                        HttpResponseStatus.BAD_REQUEST,
                                                                        ErrorResponse
                                                                                .from(ErrorCode.INVALID_REFRESH_TOKEN)
                                                                )
                                                        )
                                                )
                                )
                                .compose(
                                        session -> validateSession(sessionPayload, session)
                                                .map(session)
                                )
                )
                .compose(
                        session -> pgPool.withTransaction(
                                sqlConnection -> userDao.getById(sqlConnection, session.getUserId())
                                        .compose(
                                                userOptional -> userOptional.map(Future::succeededFuture)
                                                        .orElseGet(
                                                                () -> Future.failedFuture(
                                                                        new RestException(
                                                                                HttpResponseStatus.NOT_ACCEPTABLE,
                                                                                ErrorResponse
                                                                                        .from(ErrorCode.USER_NOT_FOUND)
                                                                        )
                                                                )
                                                        )
                                        )
                                        .compose(user -> {
                                            if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                                                return Future.failedFuture(
                                                        new RestException(
                                                                HttpResponseStatus.NOT_ACCEPTABLE,
                                                                ErrorResponse.from(ErrorCode.USER_ACCOUNT_IS_NOT_ACTIVE)
                                                        )
                                                );
                                            }
                                            return Future.succeededFuture();
                                        })
                                        .compose(
                                                obj -> roleDao.fetchRolesByUserIdAndDeleted(
                                                        sqlConnection,
                                                        session.getUserId(),
                                                        false
                                                )
                                        )

                        )
                                .compose(roles -> {
                                    session.refresh();
                                    return sessionDao.updateRefreshToken(mongoClient, session)
                                            .map(ignored -> {
                                                SessionPayload sessionPayload = SessionPayload.builder()
                                                        .sessionId(session.getId())
                                                        .refreshToken(session.getCurrentRefreshToken())
                                                        .build();

                                                String[] roleNamesArray = roles.stream()
                                                        .map(r -> r.getRoleName().name())
                                                        .toArray(String[]::new);
                                                return getLoginResponseDTO(
                                                        session.getUserId(),
                                                        roleNamesArray,
                                                        sessionPayload
                                                );
                                            });
                                })
                );
    }

    private Future<Void> validateSession(SessionPayload sessionPayload, Session session) {
        if (session.getExpiryTime() < System.currentTimeMillis()) {
            return Future.failedFuture(
                    new RestException(
                            HttpResponseStatus.NOT_ACCEPTABLE,
                            ErrorResponse.from(ErrorCode.SESSION_HAS_EXPIRED)
                    )
            );
        }
        if (session.getStatus() != SessionStatus.ACTIVE) {
            return Future.failedFuture(
                    new RestException(
                            HttpResponseStatus.NOT_ACCEPTABLE,
                            ErrorResponse.from(ErrorCode.SESSION_IS_NOT_ACTIVE)
                    )
            );
        }

        if (!session.getCurrentRefreshToken().equals(sessionPayload.refreshToken())) {
            Future<Void> future = voidSessionIfReused(sessionPayload, session);
            return future.compose(
                    v -> Future.failedFuture(
                            new RestException(
                                    HttpResponseStatus.BAD_REQUEST,
                                    ErrorResponse.from(ErrorCode.INVALID_REFRESH_TOKEN)
                            )
                    )
            );
        }
        return Future.succeededFuture();
    }

    private Future<Void> voidSessionIfReused(SessionPayload sessionPayload, Session session) {
        if (session.getUsedRefreshTokens().contains(sessionPayload.refreshToken())) {
            return sessionDao.updateStatus(
                    mongoClient,
                    session.getId(),
                    SessionStatus.VOID
            );
        }
        return Future.succeededFuture();
    }

    private LoginResponseDTO getLoginResponseDTO(Long userId, String[] roles, SessionPayload sessionPayload) {
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        JWTAuthTokenDTO jwtAuthTokenDTO = new JWTAuthTokenDTO();
        jwtAuthTokenDTO.setUserId(userId);
        jwtAuthTokenDTO.setSessionId(sessionPayload.sessionId());
        jwtAuthTokenDTO.setRoles(roles);

        loginResponseDTO.setAuthToken(jwtService.generateAuthToken(jwtAuthTokenDTO));
        loginResponseDTO.setRefreshToken(sessionPayload.createRefreshToken());
        return loginResponseDTO;
    }
}
