package com.nagpal.shivam.workout_manager_user.services.impl;

import com.nagpal.shivam.workout_manager_user.daos.RoleDao;
import com.nagpal.shivam.workout_manager_user.daos.SessionDao;
import com.nagpal.shivam.workout_manager_user.daos.UserDao;
import com.nagpal.shivam.workout_manager_user.dtos.internal.JWTAuthTokenDTO;
import com.nagpal.shivam.workout_manager_user.dtos.internal.UserUpdateRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.EmailRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.LoginRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.PasswordUpdateRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.LoginResponseDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.OTPResponseDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.UserResponseDTO;
import com.nagpal.shivam.workout_manager_user.enums.AccountStatus;
import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.helpers.UserHelper;
import com.nagpal.shivam.workout_manager_user.models.Role;
import com.nagpal.shivam.workout_manager_user.models.User;
import com.nagpal.shivam.workout_manager_user.services.OTPService;
import com.nagpal.shivam.workout_manager_user.services.SessionService;
import com.nagpal.shivam.workout_manager_user.services.UserService;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.pgclient.PgPool;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.OffsetDateTime;
import java.util.List;

public class UserServiceImpl implements UserService {
    private final PgPool pgPool;
    private final MongoClient mongoClient;
    private final UserDao userDao;
    private final OTPService otpService;
    private final SessionService sessionService;
    private final RoleDao roleDao;
    private final SessionDao sessionDao;
    private final UserHelper userHelper;

    public UserServiceImpl(PgPool pgPool, MongoClient mongoClient, UserDao userDao, OTPService otpService,
                           SessionService sessionService, RoleDao roleDao, SessionDao sessionDao,
                           UserHelper userHelper) {
        this.pgPool = pgPool;
        this.mongoClient = mongoClient;
        this.userDao = userDao;
        this.otpService = otpService;
        this.sessionService = sessionService;
        this.roleDao = roleDao;
        this.sessionDao = sessionDao;
        this.userHelper = userHelper;
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
        return pgPool.withTransaction(sqlConnection -> userHelper.getUserByEmail(sqlConnection,
                                loginRequestDTO.getEmail()
                        )
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

    @Override
    public Future<UserResponseDTO> getById(JWTAuthTokenDTO jwtAuthTokenDTO) {
        return pgPool.withTransaction(sqlConnection -> {
            Future<User> userFuture = userHelper.getUserById(sqlConnection, jwtAuthTokenDTO.getUserId());
            Future<List<Role>> rolesFuture =
                    roleDao.fetchRolesByUserIdAndDeleted(sqlConnection, jwtAuthTokenDTO.getUserId(), false);
            return CompositeFuture.all(userFuture, rolesFuture)
                    .map(compositeFuture -> {
                        User user = compositeFuture.resultAt(0);
                        List<Role> roles = compositeFuture.resultAt(1);
                        return UserResponseDTO.from(user, roles);
                    });
        });
    }

    @Override
    public Future<UserResponseDTO> update(JWTAuthTokenDTO jwtAuthTokenDTO, UserUpdateRequestDTO userUpdateRequestDTO) {
        return pgPool.withTransaction(sqlConnection -> userHelper.getUserById(sqlConnection,
                        jwtAuthTokenDTO.getUserId()
                )
                .compose(user -> {
                    user.setFirstName(userUpdateRequestDTO.getFirstName());
                    user.setLastName(userUpdateRequestDTO.getLastName());
                    return userDao.update(sqlConnection, user)
                            .map(v -> UserResponseDTO.from(user, null));
                }));
    }

    @Override
    public Future<OTPResponseDTO> updateEmail(JWTAuthTokenDTO jwtAuthTokenDTO,
                                              EmailRequestDTO emailRequestDTO) {
        return pgPool.withTransaction(sqlConnection -> userHelper.getUserById(sqlConnection,
                        jwtAuthTokenDTO.getUserId()
                )
                .compose(user -> {
                    if (user.getEmail().equals(emailRequestDTO.getEmail())) {
                        return Future.failedFuture(new ResponseException(HttpResponseStatus.NOT_ACCEPTABLE.code(),
                                MessageConstants.NEW_EMAIL_CANNOT_BE_SAME_AS_THE_OLD_EMAIL, null
                        ));
                    }
                    return otpService.triggerEmailVerification(sqlConnection, jwtAuthTokenDTO.getUserId(),
                                    emailRequestDTO.getEmail(), OTPPurpose.UPDATE_EMAIL
                            )
                            .map(otpToken -> {
                                OTPResponseDTO otpResponseDTO = new OTPResponseDTO();
                                otpResponseDTO.setOtpToken(otpToken);
                                return otpResponseDTO;
                            });
                }));
    }

    @Override
    public Future<LoginResponseDTO> updatePassword(JWTAuthTokenDTO jwtAuthTokenDTO,
                                                   PasswordUpdateRequestDTO passwordUpdateRequestDTO) {
        return pgPool.withTransaction(sqlConnection ->
                userHelper.updatePasswordAndLogOutAllSessions(sqlConnection, mongoClient, jwtAuthTokenDTO.getUserId(),
                                passwordUpdateRequestDTO
                        )
                        .compose(v -> sessionService.createNewSessionAndFormLoginResponse(mongoClient,
                                jwtAuthTokenDTO.getUserId(), jwtAuthTokenDTO.getRoles())
                        )
        );
    }


    @Override
    public Future<OTPResponseDTO> resetPassword(EmailRequestDTO emailRequestDTO) {
        return pgPool.withTransaction(sqlConnection -> userHelper.getUserByEmail(sqlConnection,
                                emailRequestDTO.getEmail()
                        )
                        .compose(user -> otpService.triggerEmailVerification(sqlConnection, user.getId(), user.getEmail(),
                                                OTPPurpose.RESET_PASSWORD
                                        )
                                        .map(otpToken -> {
                                            OTPResponseDTO otpResponseDTO = new OTPResponseDTO();
                                            otpResponseDTO.setOtpToken(otpToken);
                                            return otpResponseDTO;
                                        })
                        )
        );
    }
}
