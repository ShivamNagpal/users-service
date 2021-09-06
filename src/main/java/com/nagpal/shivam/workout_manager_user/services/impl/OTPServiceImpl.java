package com.nagpal.shivam.workout_manager_user.services.impl;

import com.nagpal.shivam.workout_manager_user.daos.OTPDao;
import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
import com.nagpal.shivam.workout_manager_user.models.OTP;
import com.nagpal.shivam.workout_manager_user.services.OTPService;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Random;

public class OTPServiceImpl implements OTPService {
    private final OTPDao otpDao;
    private Random random;

    public OTPServiceImpl(OTPDao otpDao) {
        this.otpDao = otpDao;
        this.random = new SecureRandom();
    }

    @Override
    public Future<Void> triggerEmailVerification(SqlClient sqlClient, Long userId, String email,
                                                 OTPPurpose otpPurpose) {
        return otpDao.fetchAlreadyTriggeredOTP(sqlClient, userId, email)
                .compose(otpOptional -> {
                    int otpValue = generateOTP();
                    String otpHash = hashOTP(otpValue);
                    OffsetDateTime currentTime = OffsetDateTime.now();
                    Future<OTP> saveFuture;
                    if (otpOptional.isPresent()) {
                        OTP otp = otpOptional.get();
                        otp.setCount(otp.getCount() + 1);
                        otp.setOtpHash(otpHash);
                        OffsetDateTime lastAccessTime = otp.getLastAccessTime();
                        otp.setLastAccessTime(currentTime);
                        if (otp.getCount() > Constants.OTP_RETRY_LIMIT && lastAccessTime.isBefore(currentTime)) {
                            int backOffMinutes = Constants.OTP_BACKOFF_TIME - Constants.OTP_EXPIRY_TIME;
                            otp.setLastAccessTime(currentTime.plusMinutes(backOffMinutes));
                        }
                        saveFuture = otpDao.update(sqlClient, otp).map(otp);
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
                        saveFuture = otpDao.insert(sqlClient, otp).map(otp);
                    }
                    return saveFuture;
                }).compose(otp -> Future.succeededFuture());
    }

    private int generateOTP() {
        return random.nextInt(900000) + 100000;
    }

    private String hashOTP(int otp) {
        // TODO: Implement Hashing of the OTP
        return String.valueOf(otp);
    }
}
