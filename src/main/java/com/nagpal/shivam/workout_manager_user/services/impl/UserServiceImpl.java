package com.nagpal.shivam.workout_manager_user.services.impl;

import com.nagpal.shivam.workout_manager_user.daos.RoleDao;
import com.nagpal.shivam.workout_manager_user.daos.SessionDao;
import com.nagpal.shivam.workout_manager_user.daos.UserDao;
import com.nagpal.shivam.workout_manager_user.dtos.internal.JWTAuthTokenDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.LoginRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.LoginResponseDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.OTPResponseDTO;
import com.nagpal.shivam.workout_manager_user.enums.AccountStatus;
import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.services.OTPService;
import com.nagpal.shivam.workout_manager_user.services.SessionService;
import com.nagpal.shivam.workout_manager_user.services.UserService;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.pgclient.PgPool;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.OffsetDateTime;

public class UserServiceImpl implements UserService {
    private final PgPool pgPool;
    private final MongoClient mongoClient;
    private final UserDao userDao;
    private final OTPService otpService;
    private final SessionService sessionService;
    private final RoleDao roleDao;
    private final SessionDao sessionDao;

    public UserServiceImpl(PgPool pgPool, MongoClient mongoClient, UserDao userDao, OTPService otpService,
                           SessionService sessionService, RoleDao roleDao, SessionDao sessionDao) {
        this.pgPool = pgPool;
        this.mongoClient = mongoClient;
        this.userDao = userDao;
        this.otpService = otpService;
        this.sessionService = sessionService;
        this.roleDao = roleDao;
        this.sessionDao = sessionDao;
    }

    @Override
    public Future<OTPResponseDTO> signUp(User user) {
        user.setDeleted(false);
        OffsetDateTime currentTime = OffsetDateTime.now();
        user.setTimeCreated(currentTime);
        user.setTimeLastModified(currentTime);
        user.setEmailVerified(false);
        user.setAccountStatus(AccountStatus.UNVERIFIED);

        return pgPool.withTransaction(client -> userDao.signUp(client, user)
                .compose(userId -> otpService.triggerEmailVerification(client, userId, user.getEmail(),
                                        OTPPurpose.VERIFY_USER
                                )
                                .map(otpToken -> {
                                    OTPResponseDTO otpResponseDTO = new OTPResponseDTO();
                                    otpResponseDTO.setOtpToken(otpToken);
                                    return otpResponseDTO;
                                })
                )
        );
    }

    @Override
    public Future<LoginResponseDTO> login(LoginRequestDTO loginRequestDTO) {
        return pgPool.withTransaction(sqlConnection ->
                userDao.getUserByEmail(sqlConnection, loginRequestDTO.getEmail())
                        .compose(userOptional -> {
                            if (userOptional.isEmpty()) {
                                return Future.failedFuture(new ResponseException(HttpResponseStatus.BAD_REQUEST.code(),
                                        MessageConstants.USER_NOT_FOUND,
                                        null
                                ));
                            }
                            return Future.succeededFuture(userOptional.get());
                        })
                        .compose(user -> {
                            if (!BCrypt.checkpw(loginRequestDTO.getPassword(), user.getPassword())) {
                                return Future.failedFuture(
                                        new ResponseException(HttpResponseStatus.NOT_ACCEPTABLE.code(),
                                                MessageConstants.INVALID_CREDENTIALS,
                                                null
                                        ));
                            }
                            if (user.getEmailVerified() != null && !user.getEmailVerified()) {
                                return otpService.triggerEmailVerification(sqlConnection, user.getId(), user.getEmail(),
                                                OTPPurpose.VERIFY_USER
                                        )
                                        .compose(otpToken -> {
                                            OTPResponseDTO otpResponseDTO = new OTPResponseDTO();
                                            otpResponseDTO.setOtpToken(otpToken);
                                            return Future.failedFuture(new ResponseException(
                                                    HttpResponseStatus.OK.code(),
                                                    MessageConstants.USER_ACCOUNT_IS_UNVERIFIED,
                                                    otpResponseDTO
                                            ));
                                        });
                            }
                            if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                                return Future.failedFuture(
                                        new ResponseException(HttpResponseStatus.NOT_ACCEPTABLE.code(),
                                                MessageConstants.USER_ACCOUNT_IS_NOT_ACTIVE,
                                                null
                                        ));
                            }
                            return roleDao.fetchRolesByUserIdAndDeleted(sqlConnection, user.getId(), false)
                                    .compose(roles -> {
                                        String[] rolesArray =
                                                roles.stream().map(r -> r.getRoleName().name()).toArray(String[]::new);
                                        return sessionService.createNewSessionAndFormLoginResponse(mongoClient,
                                                user.getId(), rolesArray);
                                    });
                        })
        );
    }

    @Override
    public Future<Void> logout(JWTAuthTokenDTO jwtAuthTokenDTO, boolean allSession) {
        if (allSession) {
            return sessionDao.logoutAllSessions(mongoClient, jwtAuthTokenDTO.getUserId());
        } else {
            return sessionDao.logoutSession(mongoClient, jwtAuthTokenDTO.getSessionId());
        }
    }
}
