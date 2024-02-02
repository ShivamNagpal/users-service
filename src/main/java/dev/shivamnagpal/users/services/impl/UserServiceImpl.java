package dev.shivamnagpal.users.services.impl;

import dev.shivamnagpal.users.daos.RoleDao;
import dev.shivamnagpal.users.daos.SessionDao;
import dev.shivamnagpal.users.daos.UserDao;
import dev.shivamnagpal.users.dtos.internal.JWTAuthTokenDTO;
import dev.shivamnagpal.users.dtos.internal.UserUpdateRequestDTO;
import dev.shivamnagpal.users.dtos.request.EmailRequestDTO;
import dev.shivamnagpal.users.dtos.request.LoginRequestDTO;
import dev.shivamnagpal.users.dtos.request.PasswordUpdateRequestDTO;
import dev.shivamnagpal.users.dtos.response.LoginResponseDTO;
import dev.shivamnagpal.users.dtos.response.OTPResponseDTO;
import dev.shivamnagpal.users.dtos.response.ResponseWrapper;
import dev.shivamnagpal.users.dtos.response.UserResponseDTO;
import dev.shivamnagpal.users.dtos.response.wrapper.ErrorResponse;
import dev.shivamnagpal.users.enums.AccountStatus;
import dev.shivamnagpal.users.enums.ErrorCode;
import dev.shivamnagpal.users.enums.OTPPurpose;
import dev.shivamnagpal.users.exceptions.RestException;
import dev.shivamnagpal.users.helpers.UserHelper;
import dev.shivamnagpal.users.models.Role;
import dev.shivamnagpal.users.models.User;
import dev.shivamnagpal.users.services.OTPService;
import dev.shivamnagpal.users.services.SessionService;
import dev.shivamnagpal.users.services.UserService;
import dev.shivamnagpal.users.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.sqlclient.Pool;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.OffsetDateTime;
import java.util.List;

// Temporary fix to fix SonarLint Too Many Constructor Args
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final Pool pgPool;

    private final MongoClient mongoClient;

    private final UserDao userDao;

    private final OTPService otpService;

    private final SessionService sessionService;

    private final RoleDao roleDao;

    private final SessionDao sessionDao;

    private final UserHelper userHelper;

    @Override
    public Future<OTPResponseDTO> signUp(User user) {
        user.setDeleted(false);
        OffsetDateTime currentTime = OffsetDateTime.now();
        user.setTimeCreated(currentTime);
        user.setTimeLastModified(currentTime);
        user.setEmailVerified(false);
        user.setAccountStatus(AccountStatus.UNVERIFIED);

        return pgPool.withTransaction(
                client -> userDao.signUp(client, user)
                        .compose(
                                userId -> otpService.triggerEmailVerification(
                                        client,
                                        userId,
                                        user.getEmail(),
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
    public Future<Object> login(LoginRequestDTO loginRequestDTO) {
        return pgPool.withTransaction(
                sqlConnection -> userHelper.getUserByEmail(
                        sqlConnection,
                        loginRequestDTO.getEmail()
                )
                        .compose(user -> {
                            if (!BCrypt.checkpw(loginRequestDTO.getPassword(), user.getPassword())) {
                                return Future.failedFuture(
                                        new RestException(
                                                HttpResponseStatus.NOT_ACCEPTABLE,
                                                ErrorResponse.from(ErrorCode.INVALID_CREDENTIALS)
                                        )
                                );
                            }
                            if (user.getEmailVerified() == null || !user.getEmailVerified()) {
                                return otpService.triggerEmailVerification(
                                        sqlConnection,
                                        user.getId(),
                                        user.getEmail(),
                                        OTPPurpose.VERIFY_USER
                                )
                                        .map(otpToken -> {
                                            OTPResponseDTO otpResponseDTO = new OTPResponseDTO();
                                            otpResponseDTO.setOtpToken(otpToken);
                                            return ResponseWrapper.failure(
                                                    otpResponseDTO,
                                                    MessageConstants.USER_ACCOUNT_IS_UNVERIFIED
                                            );
                                        });
                            }
                            if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                                return Future.failedFuture(
                                        new RestException(
                                                HttpResponseStatus.NOT_ACCEPTABLE,
                                                ErrorResponse.from(ErrorCode.USER_ACCOUNT_IS_NOT_ACTIVE)
                                        )
                                );
                            }
                            return roleDao.fetchRolesByUserIdAndDeleted(sqlConnection, user.getId(), false)
                                    .compose(roles -> {
                                        String[] rolesArray = roles.stream()
                                                .map(r -> r.getRoleName().name())
                                                .toArray(String[]::new);
                                        return sessionService.createNewSessionAndFormLoginResponse(
                                                mongoClient,
                                                user.getId(),
                                                rolesArray
                                        );
                                    })
                                    .map(obj -> obj);
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
            Future<List<Role>> rolesFuture = roleDao
                    .fetchRolesByUserIdAndDeleted(sqlConnection, jwtAuthTokenDTO.getUserId(), false);
            return Future.all(userFuture, rolesFuture)
                    .map(compositeFuture -> {
                        User user = compositeFuture.resultAt(0);
                        List<Role> roles = compositeFuture.resultAt(1);
                        return UserResponseDTO.from(user, roles);
                    });
        });
    }

    @Override
    public Future<UserResponseDTO> update(JWTAuthTokenDTO jwtAuthTokenDTO, UserUpdateRequestDTO userUpdateRequestDTO) {
        return pgPool.withTransaction(
                sqlConnection -> userHelper.getUserById(
                        sqlConnection,
                        jwtAuthTokenDTO.getUserId()
                )
                        .compose(user -> {
                            user.setFirstName(userUpdateRequestDTO.getFirstName());
                            user.setLastName(userUpdateRequestDTO.getLastName());
                            return userDao.update(sqlConnection, user)
                                    .map(v -> UserResponseDTO.from(user, null));
                        })
        );
    }

    @Override
    public Future<OTPResponseDTO> updateEmail(
            JWTAuthTokenDTO jwtAuthTokenDTO,
            EmailRequestDTO emailRequestDTO
    ) {
        return pgPool.withTransaction(
                sqlConnection -> userHelper.getUserById(
                        sqlConnection,
                        jwtAuthTokenDTO.getUserId()
                )
                        .compose(user -> {
                            if (user.getEmail().equals(emailRequestDTO.getEmail())) {
                                return Future.failedFuture(
                                        new RestException(
                                                HttpResponseStatus.NOT_ACCEPTABLE,
                                                ErrorResponse.from(ErrorCode.NEW_EMAIL_CANNOT_BE_SAME_AS_THE_OLD_EMAIL)
                                        )
                                );
                            }
                            return otpService.triggerEmailVerification(
                                    sqlConnection,
                                    jwtAuthTokenDTO.getUserId(),
                                    emailRequestDTO.getEmail(),
                                    OTPPurpose.UPDATE_EMAIL
                            )
                                    .map(otpToken -> {
                                        OTPResponseDTO otpResponseDTO = new OTPResponseDTO();
                                        otpResponseDTO.setOtpToken(otpToken);
                                        return otpResponseDTO;
                                    });
                        })
        );
    }

    @Override
    public Future<LoginResponseDTO> updatePassword(
            JWTAuthTokenDTO jwtAuthTokenDTO,
            PasswordUpdateRequestDTO passwordUpdateRequestDTO
    ) {
        return pgPool.withTransaction(
                sqlConnection -> userHelper.updatePasswordAndLogOutAllSessions(
                        sqlConnection,
                        mongoClient,
                        jwtAuthTokenDTO.getUserId(),
                        passwordUpdateRequestDTO
                )
                        .compose(
                                v -> sessionService.createNewSessionAndFormLoginResponse(
                                        mongoClient,
                                        jwtAuthTokenDTO.getUserId(),
                                        jwtAuthTokenDTO.getRoles()
                                )
                        )
        );
    }

    @Override
    public Future<OTPResponseDTO> resetPassword(EmailRequestDTO emailRequestDTO) {
        return pgPool.withTransaction(
                sqlConnection -> userHelper.getUserByEmail(
                        sqlConnection,
                        emailRequestDTO.getEmail()
                )
                        .compose(
                                user -> otpService
                                        .triggerEmailVerification(
                                                sqlConnection,
                                                user.getId(),
                                                user.getEmail(),
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

    @Override
    public Future<Void> deactivate(JWTAuthTokenDTO jwtAuthTokenDTO) {
        return pgPool.withTransaction(
                sqlConnection -> sessionDao.logoutAllSessions(
                        mongoClient,
                        jwtAuthTokenDTO.getUserId()
                )
                        .compose(
                                v -> userDao.updateStatus(
                                        sqlConnection,
                                        jwtAuthTokenDTO.getUserId(),
                                        AccountStatus.DEACTIVATED
                                )
                        )
        );
    }

    @Override
    public Future<Void> reactivate(LoginRequestDTO loginRequestDTO) {
        return pgPool.withTransaction(
                sqlConnection -> userHelper.getUserByEmail(sqlConnection, loginRequestDTO.getEmail())
                        .compose(user -> {
                            if (!BCrypt.checkpw(loginRequestDTO.getPassword(), user.getPassword())) {
                                return Future.failedFuture(
                                        new RestException(
                                                HttpResponseStatus.NOT_ACCEPTABLE,
                                                ErrorResponse.from(ErrorCode.INVALID_CREDENTIALS)
                                        )
                                );
                            }
                            if (
                                user.getAccountStatus() != AccountStatus.DEACTIVATED &&
                                        user.getAccountStatus() != AccountStatus.SCHEDULED_FOR_DELETION
                            ) {
                                return Future.failedFuture(
                                        new RestException(
                                                HttpResponseStatus.NOT_ACCEPTABLE,
                                                ErrorResponse.from(
                                                        ErrorCode.USER_ACCOUNT_WASN_T_DEACTIVATED_OR_MARKED_FOR_DELETION
                                                )
                                        )
                                );
                            }
                            return userDao.updateStatus(sqlConnection, user.getId(), AccountStatus.ACTIVE);
                        })
        );
    }

    @Override
    public Future<Void> scheduleForDeletion(JWTAuthTokenDTO jwtAuthTokenDTO) {
        return pgPool.withTransaction(
                sqlConnection -> sessionDao.logoutAllSessions(
                        mongoClient,
                        jwtAuthTokenDTO.getUserId()
                )
                        .compose(
                                v -> userDao.updateStatus(
                                        sqlConnection,
                                        jwtAuthTokenDTO.getUserId(),
                                        AccountStatus.SCHEDULED_FOR_DELETION
                                )
                        )
        );
    }
}
