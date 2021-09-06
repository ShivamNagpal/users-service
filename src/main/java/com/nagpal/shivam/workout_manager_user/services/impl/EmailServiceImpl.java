package com.nagpal.shivam.workout_manager_user.services.impl;

import com.nagpal.shivam.workout_manager_user.enums.Configuration;
import com.nagpal.shivam.workout_manager_user.services.EmailService;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;

import java.text.MessageFormat;

public class EmailServiceImpl implements EmailService {
    private final MailClient mailClient;
    private final JsonObject config;

    public EmailServiceImpl(MailClient mailClient, JsonObject config) {
        this.mailClient = mailClient;
        this.config = config;
    }

    @Override
    public Future<MailResult> sendOTPEmail(String emailId, int otp) {
        String mailContent = MessageFormat.format(MessageConstants.OTP_EMAIL_CONTENT_FORMAT, String.valueOf(otp));
        MailMessage mailMessage = new MailMessage()
                .setTo(emailId)
                .setFrom(config.getString(Configuration.MAIL_USERNAME.getKey()))
                .setSubject(MessageConstants.OTP_EMAIL_SUBJECT)
                .setText(mailContent);
        return mailClient.sendMail(mailMessage);
    }
}
