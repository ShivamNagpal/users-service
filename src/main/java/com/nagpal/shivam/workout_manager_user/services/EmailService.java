package com.nagpal.shivam.workout_manager_user.services;

import io.vertx.core.Future;
import io.vertx.ext.mail.MailResult;

public interface EmailService {
    Future<MailResult> sendOTPEmail(String emailId, int otp);
}
