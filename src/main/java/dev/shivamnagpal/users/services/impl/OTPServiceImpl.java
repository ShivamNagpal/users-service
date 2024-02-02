package dev.shivamnagpal.users.services.impl;

import dev.shivamnagpal.users.daos.OTPDao;
import dev.shivamnagpal.users.daos.RoleDao;
import dev.shivamnagpal.users.daos.UserDao;
import dev.shivamnagpal.users.dtos.internal.JWTOTPTokenDTO;
import dev.shivamnagpal.users.dtos.request.VerifyOTPRequestDTO;
import dev.shivamnagpal.users.dtos.response.OTPResponseDTO;
import dev.shivamnagpal.users.dtos.response.wrapper.ErrorResponse;
import dev.shivamnagpal.users.enums.ErrorCode;
import dev.shivamnagpal.users.enums.OTPPurpose;
import dev.shivamnagpal.users.enums.OTPStatus;
import dev.shivamnagpal.users.enums.RoleName;
import dev.shivamnagpal.users.exceptions.RestException;
import dev.shivamnagpal.users.helpers.UserHelper;
import dev.shivamnagpal.users.models.OTP;
import dev.shivamnagpal.users.services.EmailService;
import dev.shivamnagpal.users.services.JWTService;
import dev.shivamnagpal.users.services.OTPService;
import dev.shivamnagpal.users.services.SessionService;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.security.SecureRandom;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.Random;

// Temporary fix to fix SonarLint Too Many Constructor Args
@RequiredArgsConstructor
public class OTPServiceImpl implements OTPService {
    private final JsonObject config;

    private final Pool pgPool;

    private final MongoClient mongoClient;

    private final OTPDao otpDao;

    private final EmailService emailService;

    private final SessionService sessionService;

    private final JWTService jwtService;

    private final UserHelper userHelper;

    private final UserDao userDao;

    private final RoleDao roleDao;

    private final Random random = new SecureRandom();

    @Override
    public Future<OTPResponseDTO> resendOTP(JWTOTPTokenDTO jwtotpTokenDTO) {
        return triggerEmailVerification(
                pgPool,
                jwtotpTokenDTO.getUserId(),
                jwtotpTokenDTO.getEmail(),
                jwtotpTokenDTO.getOtpPurpose()
        )
                .map(newOtpToken -> {
                    OTPResponseDTO otpResponseDTO = new OTPResponseDTO();
                    otpResponseDTO.setOtpToken(newOtpToken);
                    return otpResponseDTO;
                });
    }

    @Override
    public Future<Object> verifyOTP(JWTOTPTokenDTO jwtotpTokenDTO, VerifyOTPRequestDTO verifyOTPRequestDTO) {
        return pgPool.withTransaction(
                sqlConnection -> otpDao.fetchActiveOTP(
                        sqlConnection,
                        jwtotpTokenDTO.getUserId(),
                        jwtotpTokenDTO.getEmail(),
                        jwtotpTokenDTO.getOtpPurpose()
                )
                        .compose(otpOptional -> {
                            if (otpOptional.isEmpty()) {
                                return Future.failedFuture(
                                        new RestException(
                                                HttpResponseStatus.NOT_ACCEPTABLE,
                                                ErrorResponse.from(ErrorCode.NO_ACTIVE_TRIGGERED_OTP_FOUND)
                                        )
                                );
                            }
                            OTP otp = otpOptional.get();
                            if (!BCrypt.checkpw(String.valueOf(verifyOTPRequestDTO.getOtp()), otp.getOtpHash())) {
                                return Future.failedFuture(
                                        new RestException(
                                                HttpResponseStatus.NOT_ACCEPTABLE,
                                                ErrorResponse.from(ErrorCode.INCORRECT_OTP)
                                        )
                                );
                            }
                            return Future.succeededFuture(otp);
                        })
                        .compose(otp -> otpDao.updateOTPStatus(sqlConnection, otp.getId(), OTPStatus.USED))
                        .compose(v -> {
                            Future<Object> actionPostOTPVerification;
                            switch (jwtotpTokenDTO.getOtpPurpose()) {
                                case VERIFY_USER:
                                    actionPostOTPVerification = userDao.activateUser(
                                            sqlConnection,
                                            jwtotpTokenDTO.getUserId()
                                    )
                                            .compose(
                                                    v2 -> roleDao.insertRole(
                                                            sqlConnection,
                                                            jwtotpTokenDTO.getUserId(),
                                                            RoleName.USER
                                                    )
                                            )
                                            .compose(
                                                    id -> sessionService.createNewSessionAndFormLoginResponse(
                                                            mongoClient,
                                                            jwtotpTokenDTO.getUserId(),
                                                            new String[] { RoleName.USER.name() }
                                                    )
                                            )
                                            .map(loginResponseDTO -> loginResponseDTO);
                                    break;
                                case UPDATE_EMAIL:
                                    actionPostOTPVerification = userDao.updateEmail(
                                            sqlConnection,
                                            jwtotpTokenDTO.getUserId(),
                                            jwtotpTokenDTO.getEmail()
                                    )
                                            .compose(v2 -> Future.succeededFuture());
                                    break;
                                case RESET_PASSWORD:
                                    actionPostOTPVerification = userHelper.getUserById(
                                            sqlConnection,
                                            jwtotpTokenDTO.getUserId()
                                    )
                                            .compose(user -> {
                                                Future<Void> future = userHelper.updatePasswordAndLogOutAllSessions(
                                                        sqlConnection,
                                                        mongoClient,
                                                        user,
                                                        verifyOTPRequestDTO
                                                );
                                                if (user.getEmailVerified() == null || !user.getEmailVerified()) {
                                                    future = future.compose(
                                                            v2 -> userDao.activateUser(
                                                                    sqlConnection,
                                                                    user.getId()
                                                            )
                                                    );
                                                }
                                                return future;
                                            })
                                            .compose(v2 -> Future.succeededFuture());
                                    break;
                                default:
                                    actionPostOTPVerification = Future.failedFuture(
                                            MessageFormat.format(
                                                    MessageConstants.POST_VERIFICATION_ACTION_NOT_MAPPED_FOR_THE_OTP_PURPOSE,
                                                    jwtotpTokenDTO.getOtpPurpose()
                                            )
                                    );
                            }
                            return actionPostOTPVerification;
                        })
        );
    }

    @Override
    public Future<String> triggerEmailVerification(
            SqlClient sqlClient,
            Long userId,
            String email,
            OTPPurpose otpPurpose
    ) {
        return otpDao.fetchAlreadyTriggeredOTP(sqlClient, userId, email, otpPurpose)
                .compose(otpOptional -> {
                    int otpValue = generateOTP();
                    String otpHash = hashOTP(otpValue);
                    OffsetDateTime currentTime = OffsetDateTime.now();
                    Future<Integer> saveFuture;
                    if (otpOptional.isPresent()) {
                        OTP otp = otpOptional.get();
                        otp.setCount(otp.getCount() + 1);
                        otp.setOtpHash(otpHash);
                        otp.setValidAfter(currentTime);
                        if (otp.getCount() > config.getInteger(Constants.OTP_RETRY_LIMIT)) {
                            Future<Void> future = Future.succeededFuture();
                            if (otp.getOtpStatus() == OTPStatus.ACTIVE) {
                                int backOffMinutes = config.getInteger(Constants.OTP_BACKOFF_TIME) -
                                        config.getInteger(Constants.OTP_EXPIRY_TIME);
                                otp.setValidAfter(currentTime.plusSeconds(backOffMinutes));
                                otp.setOtpStatus(OTPStatus.OTP_RESEND_LIMIT_REACHED);
                                future = otpDao.update(sqlClient, otp);
                            }
                            return future.compose(
                                    v -> Future.failedFuture(
                                            new RestException(
                                                    HttpResponseStatus.NOT_ACCEPTABLE,
                                                    ErrorResponse.from(ErrorCode.OTP_RESEND_LIMIT_EXCEEDED)
                                            )
                                    )
                            );
                        }
                        saveFuture = otpDao.update(sqlClient, otp).map(otpValue);
                    } else {
                        OTP otp = new OTP();
                        otp.setUserId(userId);
                        otp.setEmail(email);
                        otp.setOtpHash(otpHash);
                        otp.setCount(1);
                        otp.setValidAfter(currentTime);
                        otp.setPurpose(otpPurpose);
                        otp.setDeleted(false);
                        otp.setTimeCreated(currentTime);
                        otp.setOtpStatus(OTPStatus.ACTIVE);
                        saveFuture = otpDao.insert(sqlClient, otp).map(otpValue);
                    }
                    return saveFuture;
                })
                .compose(otpValue -> {
                    emailService.sendOTPEmail(email, otpValue);
                    JWTOTPTokenDTO jwtotpTokenDTO = new JWTOTPTokenDTO();
                    jwtotpTokenDTO.setUserId(userId);
                    jwtotpTokenDTO.setEmail(email);
                    jwtotpTokenDTO.setOtpPurpose(otpPurpose);
                    String otpToken = jwtService.generateOTPToken(jwtotpTokenDTO);
                    return Future.succeededFuture(otpToken);
                });
    }

    private int generateOTP() {
        return random.nextInt(900000) + 100000;
    }

    private String hashOTP(int otp) {
        return BCrypt.hashpw(String.valueOf(otp), BCrypt.gensalt(config.getInteger(Constants.BCRYPT_OTP_LOG_ROUNDS)));
    }
}
