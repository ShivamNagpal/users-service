package com.nagpal.shivam.workout_manager_user.verticles;

import com.nagpal.shivam.workout_manager_user.configurations.DatabaseConfiguration;
import com.nagpal.shivam.workout_manager_user.configurations.EmailConfiguration;
import com.nagpal.shivam.workout_manager_user.controllers.HealthController;
import com.nagpal.shivam.workout_manager_user.controllers.OTPController;
import com.nagpal.shivam.workout_manager_user.controllers.UserController;
import com.nagpal.shivam.workout_manager_user.daos.HealthDao;
import com.nagpal.shivam.workout_manager_user.daos.OTPDao;
import com.nagpal.shivam.workout_manager_user.daos.RoleDao;
import com.nagpal.shivam.workout_manager_user.daos.UserDao;
import com.nagpal.shivam.workout_manager_user.daos.impl.HealthDaoImpl;
import com.nagpal.shivam.workout_manager_user.daos.impl.OTPDaoImpl;
import com.nagpal.shivam.workout_manager_user.daos.impl.RoleDaoImpl;
import com.nagpal.shivam.workout_manager_user.daos.impl.UserDaoImpl;
import com.nagpal.shivam.workout_manager_user.enums.Configuration;
import com.nagpal.shivam.workout_manager_user.services.*;
import com.nagpal.shivam.workout_manager_user.services.impl.*;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgPool;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = Logger.getLogger(MainVerticle.class.getName());
    private Router mainRouter;
    private PgPool pgPool;
    private MongoClient mongoClient;

    public static Future<String> deploy(Vertx vertx, JsonObject config) {
        DeploymentOptions httpDeploymentOptions = new DeploymentOptions()
                .setInstances(2 * config.getInteger(Constants.AVAILABLE_PROCESSORS))
                .setConfig(config);
        return vertx.deployVerticle(MainVerticle.class.getName(), httpDeploymentOptions)
                .onSuccess(result -> logger.log(Level.INFO,
                        MessageFormat.format(MessageConstants.SERVER_STARTED_ON_PORT,
                                String.valueOf(config.getInteger(Configuration.SERVER_PORT.getKey())))));
    }

    @Override
    public void start(Promise<Void> startPromise) {
        String startVerticleMessage =
                MessageFormat.format(MessageConstants.STARTING_VERTICLE, this.getClass().getSimpleName());
        logger.info(startVerticleMessage);
        JsonObject config = this.config();
        this.setupDBClients(vertx, config)
                .compose(v -> this.setupHttpServer(vertx, config))
                .map(v -> {
                    initComponents(config);
                    return true;
                })
                .onSuccess(a -> startPromise.complete())
                .onFailure(startPromise::fail);
    }

    private Future<Void> setupDBClients(Vertx vertx, JsonObject config) {
        pgPool = DatabaseConfiguration.getSqlClient(vertx, config);
        mongoClient = DatabaseConfiguration.getMongoClient(vertx, config);
        HealthDaoImpl healthDao = new HealthDaoImpl();
        return CompositeFuture.all(healthDao.pgPoolHealthCheck(pgPool),
                        healthDao.mongoClientHealthCheck(mongoClient))
                .compose(compositeFuture -> Future.succeededFuture());
    }

    private Future<Void> setupHttpServer(Vertx vertx, JsonObject config) {
        Promise<Void> promise = Promise.promise();
        mainRouter = Router.router(vertx);
        vertx.createHttpServer()
                .requestHandler(mainRouter)
                .listen(config.getInteger(Configuration.SERVER_PORT.getKey()), http -> {
                    if (http.succeeded()) {
                        promise.complete();
                    } else {
                        promise.fail(http.cause());
                    }
                });
        return promise.future();
    }

    private void initComponents(JsonObject config) {
        setupFilters();
        HealthDao healthDao = new HealthDaoImpl();
        UserDao userDao = new UserDaoImpl();
        OTPDao otpDao = new OTPDaoImpl(config);
        RoleDao roleDao = new RoleDaoImpl();

        HealthService healthService = new HealthServiceImpl(pgPool, mongoClient, healthDao);
        JWTService jwtService = new JWTServiceImpl(config);
        EmailService emailService = new EmailServiceImpl(EmailConfiguration.getMailClient(vertx, config), config);
        OTPService otpService = new OTPServiceImpl(config, pgPool, otpDao, emailService, jwtService, userDao, roleDao);
        UserService userService = new UserServiceImpl(pgPool, mongoClient, userDao, otpService);

        new HealthController(vertx, mainRouter, healthService);
        new OTPController(vertx, mainRouter, otpService, jwtService);
        new UserController(vertx, config, mainRouter, userService);
    }

    private void setupFilters() {
        mainRouter.route()
                .handler(BodyHandler.create());
        mainRouter.route().handler(routingContext -> {
            routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
            routingContext.next();
        });
    }
}
