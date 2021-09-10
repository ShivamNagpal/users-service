package com.nagpal.shivam.workout_manager_user.services;

import com.nagpal.shivam.workout_manager_user.enums.OTPPurpose;

public interface JWTService {
    String generateOTPToken(Long userId, String email, OTPPurpose otpPurpose);
}
