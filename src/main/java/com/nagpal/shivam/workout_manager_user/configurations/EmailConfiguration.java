package com.nagpal.shivam.workout_manager_user.configurations;

import com.nagpal.shivam.workout_manager_user.enums.Configuration;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;

public class EmailConfiguration {

    private EmailConfiguration() {
    }

    public static MailClient getMailClient(Vertx vertx, JsonObject config) {
        MailConfig mailConfig = new MailConfig()
                .setHostname(config.getString(Constants.MAIL_HOST))
                .setPort(config.getInteger(Constants.MAIL_PORT))
                .setUsername(config.getString(Configuration.MAIL_USERNAME.getKey()))
                .setPassword(config.getString(Configuration.MAIL_PASSWORD.getKey()));
        return MailClient.createShared(vertx, mailConfig);
    }
}
