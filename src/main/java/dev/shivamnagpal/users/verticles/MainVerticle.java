package dev.shivamnagpal.users.verticles;

import dev.shivamnagpal.users.configurations.DatabaseConfiguration;
import dev.shivamnagpal.users.configurations.EmailConfiguration;
import dev.shivamnagpal.users.controllers.*;
import dev.shivamnagpal.users.core.RequestPath;
import dev.shivamnagpal.users.enums.Configuration;
import dev.shivamnagpal.users.exceptions.handlers.GlobalExceptionHandler;
import dev.shivamnagpal.users.helpers.UserHelper;
import dev.shivamnagpal.users.services.*;
import dev.shivamnagpal.users.services.impl.*;
import dev.shivamnagpal.users.utils.AuthenticationUtils;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.MessageConstants;
import dev.shivamnagpal.users.daos.*;
import dev.shivamnagpal.users.daos.impl.*;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.sqlclient.Pool;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class MainVerticle extends AbstractVerticle {

    private Router router;

    private Pool pgPool;

    private MongoClient mongoClient;

    public static Future<String> deploy(Vertx vertx, JsonObject config) {
        DeploymentOptions httpDeploymentOptions = new DeploymentOptions()
                .setInstances(2 * config.getInteger(Constants.AVAILABLE_PROCESSORS))
                .setConfig(config);
        return vertx.deployVerticle(MainVerticle.class.getName(), httpDeploymentOptions)
                .onSuccess(
                        result -> log.info(
                                MessageFormat.format(
                                        MessageConstants.SERVER_STARTED_ON_PORT,
                                        String.valueOf(config.getInteger(Configuration.SERVER_PORT.getKey()))
                                )
                        )
                );
    }

    @Override
    public void start(Promise<Void> startPromise) {
        String startVerticleMessage = MessageFormat
                .format(MessageConstants.STARTING_VERTICLE, this.getClass().getSimpleName());
        log.info(startVerticleMessage);
        JsonObject config = this.config();
        this.setupDBClients(vertx, config)
                .compose(v -> this.setupHttpServer(vertx, config))
                .compose(v -> {
                    initComponents(config);
                    return Future.succeededFuture();
                })
                .onSuccess(a -> startPromise.complete())
                .onFailure(startPromise::fail);
    }

    private Future<Void> setupDBClients(Vertx vertx, JsonObject config) {
        pgPool = DatabaseConfiguration.getPostgresPool(vertx, config);
        mongoClient = DatabaseConfiguration.getMongoClient(vertx, config);
        HealthDaoImpl healthDao = new HealthDaoImpl();
        return Future.all(
                healthDao.pgPoolHealthCheck(pgPool),
                healthDao.mongoClientHealthCheck(mongoClient)
        )
                .compose(compositeFuture -> Future.succeededFuture());
    }

    private Future<Void> setupHttpServer(Vertx vertx, JsonObject config) {
        Promise<Void> promise = Promise.promise();
        router = Router.router(vertx);
        vertx.createHttpServer()
                .requestHandler(router)
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
        JWTService jwtService = new JWTServiceImpl(config);
        setupFilters(jwtService);

        HealthDao healthDao = new HealthDaoImpl();
        UserDao userDao = new UserDaoImpl(config);
        OTPDao otpDao = new OTPDaoImpl(config);
        RoleDao roleDao = new RoleDaoImpl();
        SessionDao sessionDao = new SessionDaoImpl();

        UserHelper userHelper = new UserHelper(config, userDao, sessionDao);

        HealthService healthService = new HealthServiceImpl(pgPool, mongoClient, healthDao);
        SessionService sessionService = new SessionServiceImpl(
                pgPool, mongoClient, config, sessionDao, jwtService,
                userDao, roleDao
        );
        EmailService emailService = new EmailServiceImpl(EmailConfiguration.getMailClient(vertx, config), config);
        OTPService otpService = new OTPServiceImpl(
                config, pgPool, mongoClient, otpDao, emailService, sessionService,
                jwtService, userHelper, userDao, roleDao
        );
        UserService userService = new UserServiceImpl(
                pgPool, mongoClient, userDao, otpService, sessionService, roleDao,
                sessionDao, userHelper
        );
        RoleService roleService = new RoleServiceImpl(pgPool, roleDao, userHelper);
        RequestPath requestPath = new RequestPath("");
        new SessionController(router, requestPath, sessionService);
        new HealthController(router, requestPath, healthService);
        new OTPController(router, requestPath, config, otpService, jwtService);
        new UserController(router, requestPath, config, userService, jwtService);
        new RoleController(router, requestPath, roleService, jwtService);
    }

    private void setupFilters(JWTService jwtService) {
        router.route().handler(BodyHandler.create());
        router.route().handler(routingContext -> {
            routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);

            AuthenticationUtils.authenticate(routingContext.request(), jwtService)
                    .onSuccess(v -> routingContext.next())
                    .onFailure(throwable -> GlobalExceptionHandler.handle(throwable, routingContext.response()));
        });
    }

}
