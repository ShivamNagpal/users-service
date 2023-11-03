package dev.shivamnagpal.users.utils;

import dev.shivamnagpal.users.configurations.DatabaseConfiguration;
import dev.shivamnagpal.users.daos.SessionDao;
import dev.shivamnagpal.users.daos.UserDao;
import dev.shivamnagpal.users.daos.impl.SessionDaoImpl;
import dev.shivamnagpal.users.daos.impl.UserDaoImpl;
import dev.shivamnagpal.users.helpers.UserHelper;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PeriodicTasksUtil {
    private static final Logger logger = Logger.getLogger(PeriodicTasksUtil.class.getName());

    private PeriodicTasksUtil() {
    }

    public static Future<Void> setupPeriodicTasks(Vertx vertx, JsonObject config) {
        UserDao userDao = new UserDaoImpl(config);
        SessionDao sessionDao = new SessionDaoImpl();
        UserHelper userHelper = new UserHelper(config, userDao, sessionDao);
        PgPool pgPool = DatabaseConfiguration.getSqlClient(vertx, config);

        setupPeriodicTasks(vertx, config, pgPool, userHelper);
        return Future.succeededFuture();
    }

    private static void setupPeriodicTasks(Vertx vertx, JsonObject config, PgPool pgPool, UserHelper userHelper) {
        setupDeletionCron(vertx, config, pgPool, userHelper);
    }

    private static void setupDeletionCron(Vertx vertx, JsonObject config, PgPool pgPool, UserHelper userHelper) {
        vertx.setPeriodic(
                config.getLong(Constants.DELETION_CRON_DELAY),
                id -> userHelper.deleteScheduleAccount(pgPool)
                        .onSuccess(v -> logger.log(Level.INFO, MessageConstants.DELETION_CRON_EXECUTED_SUCCESSFULLY))
                        .onFailure(
                                throwable -> logger.log(Level.SEVERE, MessageConstants.DELETION_CRON_FAILED, throwable)
                        )
        );
    }
}
