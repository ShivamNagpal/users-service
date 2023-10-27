package dev.shivamnagpal.users.configurations;

import dev.shivamnagpal.users.enums.Configuration;
import dev.shivamnagpal.users.utils.Constants;
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
