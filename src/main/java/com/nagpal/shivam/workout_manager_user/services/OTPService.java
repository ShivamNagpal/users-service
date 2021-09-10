package com.nagpal.shivam.workout_manager_user.services;

import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

public interface OTPService {
    Future<String> triggerEmailVerification(SqlClient sqlClient, Long userId, String email, OTPPurpose otpPurpose);
}
