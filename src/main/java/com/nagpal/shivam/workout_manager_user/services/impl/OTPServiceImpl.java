package com.nagpal.shivam.workout_manager_user.services.impl;

import com.nagpal.shivam.workout_manager_user.daos.OTPDao;
import com.nagpal.shivam.workout_manager_user.daos.RoleDao;
import com.nagpal.shivam.workout_manager_user.daos.UserDao;
import com.nagpal.shivam.workout_manager_user.dtos.internal.JWTOTPTokenDTO;
import com.nagpal.shivam.workout_manager_user.dtos.request.VerifyOTPRequestDTO;
import com.nagpal.shivam.workout_manager_user.dtos.response.OTPResponseDTO;
import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.models.OTP;
import com.nagpal.shivam.workout_manager_user.services.EmailService;
import com.nagpal.shivam.workout_manager_user.services.JWTService;
import com.nagpal.shivam.workout_manager_user.services.OTPService;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlClient;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.security.SecureRandom;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.Random;

public class OTPServiceImpl implements OTPService {
    private final PgPool pgPool;
    private final OTPDao otpDao;
    private final EmailService emailService;
    private final JWTService jwtService;
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final Random random;

    public OTPServiceImpl(PgPool pgPool, OTPDao otpDao, EmailService emailService,
                          JWTService jwtService, UserDao userDao, RoleDao roleDao) {
        this.pgPool = pgPool;
        this.otpDao = otpDao;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.random = new SecureRandom();
    }

    @Override
    public Future<OTPResponseDTO> resendOTP(JWTOTPTokenDTO jwtotpTokenDTO) {
        return triggerEmailVerification(pgPool, jwtotpTokenDTO.getUserId(), jwtotpTokenDTO.getEmail(),
                jwtotpTokenDTO.getOtpPurpose())
                .map(newOtpToken -> {
                    OTPResponseDTO otpResponseDTO = new OTPResponseDTO();
                    otpResponseDTO.setOtpToken(newOtpToken);
                    return otpResponseDTO;
                });
    }

    @Override
    public Future<Void> verifyOTP(JWTOTPTokenDTO jwtotpTokenDTO, VerifyOTPRequestDTO verifyOTPRequestDTO) {
        return pgPool.withTransaction(sqlConnection -> otpDao.fetchActiveOTP(
                                sqlConnection,
                                jwtotpTokenDTO.getUserId(),
                                jwtotpTokenDTO.getEmail()
                        )
                        .compose(otpOptional -> {
                            if (otpOptional.isEmpty()) {
                                return Future.failedFuture(new ResponseException(HttpResponseStatus.NOT_ACCEPTABLE.code(),
                                        MessageConstants.NO_ACTIVE_TRIGGERED_OTP_FOUND, null
                                ));
                            }
                            OTP otp = otpOptional.get();
                            if (!BCrypt.checkpw(String.valueOf(verifyOTPRequestDTO.getOtp()), otp.getOtpHash())) {
                                return Future.failedFuture(new ResponseException(HttpResponseStatus.NOT_ACCEPTABLE.code(),
                                        MessageConstants.INCORRECT_OTP, null
                                ));
                            }
                            return Future.succeededFuture();
                        })
                        .compose(v -> {
                            Future<Void> actionPostOTPVerification;
                            switch (jwtotpTokenDTO.getOtpPurpose()) {
                                case VERIFY_USER:
                                    actionPostOTPVerification = userDao.activateUser(
                                                    sqlConnection,
                                                    jwtotpTokenDTO.getUserId()
                                            )
                                            .compose(v2 -> roleDao.insertUserRole(sqlConnection, jwtotpTokenDTO.getUserId()))
                                            // TODO: Generate JWT Token and Refresh Token
                                            .compose(id -> Future.succeededFuture());
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
    public Future<String> triggerEmailVerification(SqlClient sqlClient, Long userId, String email,
                                                   OTPPurpose otpPurpose) {
        return otpDao.fetchAlreadyTriggeredOTP(sqlClient, userId, email)
                .compose(otpOptional -> {
                    int otpValue = generateOTP();
                    String otpHash = hashOTP(otpValue);
                    OffsetDateTime currentTime = OffsetDateTime.now();
                    Future<Integer> saveFuture;
                    if (otpOptional.isPresent()) {
                        OTP otp = otpOptional.get();
                        otp.setCount(otp.getCount() + 1);
                        otp.setOtpHash(otpHash);
                        OffsetDateTime lastAccessTime = otp.getLastAccessTime();
                        otp.setLastAccessTime(currentTime);
                        if (otp.getCount() > Constants.OTP_RETRY_LIMIT) {
                            Future<Void> future = Future.succeededFuture();
                            if (lastAccessTime.isBefore(currentTime)) {
                                int backOffMinutes = Constants.OTP_BACKOFF_TIME - Constants.OTP_EXPIRY_TIME;
                                otp.setLastAccessTime(currentTime.plusMinutes(backOffMinutes));
                                future = otpDao.update(sqlClient, otp);
                            }
                            return future.compose(v -> Future.failedFuture(
                                    new ResponseException(HttpResponseStatus.NOT_ACCEPTABLE.code(),
                                            MessageConstants.OTP_RESEND_LIMIT_EXCEEDED, null)));
                        }
                        saveFuture = otpDao.update(sqlClient, otp).map(otpValue);
                    } else {
                        OTP otp = new OTP();
                        otp.setUserId(userId);
                        otp.setEmail(email);
                        otp.setOtpHash(otpHash);
                        otp.setCount(1);
                        otp.setLastAccessTime(currentTime);
                        otp.setPurpose(otpPurpose);
                        otp.setDeleted(false);
                        otp.setTimeCreated(currentTime);
                        saveFuture = otpDao.insert(sqlClient, otp).map(otpValue);
                    }
                    return saveFuture;
                }).compose(otpValue -> {
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
        return BCrypt.hashpw(String.valueOf(otp), BCrypt.gensalt(Constants.BCRYPT_OTP_LOG_ROUNDS));
    }
}
