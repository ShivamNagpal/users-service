package dev.shivamnagpal.users.daos;

import dev.shivamnagpal.users.enums.OTPPurpose;
import dev.shivamnagpal.users.enums.OTPStatus;
import dev.shivamnagpal.users.models.OTP;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

import java.util.Optional;

public interface OTPDao {
    Future<Optional<OTP>> fetchAlreadyTriggeredOTP(
            SqlClient sqlClient,
            Long userId,
            String email,
            OTPPurpose otpPurpose
    );

    Future<Optional<OTP>> fetchActiveOTP(
            SqlClient sqlClient,
            Long userId,
            String email,
            OTPPurpose otpPurpose
    );

    Future<Void> update(SqlClient sqlClient, OTP otp);

    Future<Long> insert(SqlClient sqlClient, OTP otp);

    Future<Void> updateOTPStatus(SqlClient sqlClient, Long otpId, OTPStatus otpStatus);
}
