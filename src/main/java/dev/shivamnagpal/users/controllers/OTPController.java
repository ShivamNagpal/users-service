package dev.shivamnagpal.users.controllers;

import dev.shivamnagpal.users.dtos.request.VerifyOTPRequestDTO;
import dev.shivamnagpal.users.dtos.response.ResponseWrapper;
import dev.shivamnagpal.users.exceptions.ResponseException;
import dev.shivamnagpal.users.exceptions.handlers.GlobalExceptionHandler;
import dev.shivamnagpal.users.services.JWTService;
import dev.shivamnagpal.users.services.OTPService;
import dev.shivamnagpal.users.utils.Constants;
import dev.shivamnagpal.users.utils.MessageConstants;
import dev.shivamnagpal.users.utils.RequestValidationUtils;
import dev.shivamnagpal.users.utils.RoutingConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class OTPController {
    private final Router router;
    private final JsonObject config;
    private final OTPService otpService;
    private final JWTService jwtService;

    public OTPController(Vertx vertx, JsonObject config, Router mainRouter, OTPService otpService,
                         JWTService jwtService) {
        this.router = Router.router(vertx);
        this.config = config;
        this.jwtService = jwtService;
        mainRouter.mountSubRouter(RoutingConstants.OTP, router);
        this.otpService = otpService;

        setupEndpoints();
    }

    private void setupEndpoints() {
        resendOTP();
        verifyOTP();
    }

    private void resendOTP() {
        router.post(RoutingConstants.RESEND_OTP)
                .handler(routingContext -> {
                    String otpToken = routingContext.request().getHeader(Constants.OTP_TOKEN);
                    if (otpToken == null) {
                        ResponseException exception = new ResponseException(HttpResponseStatus.BAD_REQUEST.code(),
                                MessageConstants.OTP_TOKEN_NOT_PROVIDED,
                                null
                        );
                        GlobalExceptionHandler.handle(exception, routingContext.response());
                        return;
                    }
                    jwtService.verifyAndDecodeOTPToken(otpToken)
                            .compose(otpService::resendOTP)
                            .onSuccess(otpResponseDTO -> routingContext.response()
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(ResponseWrapper.success(otpResponseDTO))))
                            .onFailure(throwable -> GlobalExceptionHandler.handle(throwable,
                                    routingContext.response()));
                });
    }

    private void verifyOTP() {
        router.post(RoutingConstants.VERIFY_OTP)
                .handler(routingContext -> {
                    String otpToken = routingContext.request().getHeader(Constants.OTP_TOKEN);
                    if (otpToken == null) {
                        ResponseException exception = new ResponseException(HttpResponseStatus.BAD_REQUEST.code(),
                                MessageConstants.OTP_TOKEN_NOT_PROVIDED,
                                null
                        );
                        GlobalExceptionHandler.handle(exception, routingContext.response());
                        return;
                    }
                    jwtService.verifyAndDecodeOTPToken(otpToken)
                            .compose(jWTOTPTokenDTO -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                                    .compose(body -> VerifyOTPRequestDTO.fromRequest(body, config,
                                            jWTOTPTokenDTO.getOtpPurpose())
                                    )
                                    .compose(verifyOTPRequestDTO -> otpService.verifyOTP(jWTOTPTokenDTO,
                                            verifyOTPRequestDTO
                                    ))
                            )
                            .onSuccess(obj -> routingContext.response()
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                            )
                            .onFailure(throwable -> GlobalExceptionHandler.handle(throwable,
                                    routingContext.response()
                            ));
                });
    }
}
