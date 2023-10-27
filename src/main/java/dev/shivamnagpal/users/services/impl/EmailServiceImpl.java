package dev.shivamnagpal.users.services.impl;

import dev.shivamnagpal.users.enums.Configuration;
import dev.shivamnagpal.users.services.EmailService;
import dev.shivamnagpal.users.utils.MessageConstants;
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
