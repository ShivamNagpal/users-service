package com.nagpal.shivam.workout_manager_user.daos;

import com.nagpal.shivam.workout_manager_user.models.OTP;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

import java.util.Optional;

public interface OTPDao {
    Future<Optional<OTP>> fetchAlreadyTriggeredOTP(SqlClient sqlClient, Long userId, String email);

    Future<Optional<OTP>> fetchActiveOTP(SqlClient sqlClient, Long userId, String email);

    Future<Void> update(SqlClient sqlClient, OTP otp);

    Future<Long> insert(SqlClient sqlClient, OTP otp);
}
