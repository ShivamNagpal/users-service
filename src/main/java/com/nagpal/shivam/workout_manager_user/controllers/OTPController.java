package com.nagpal.shivam.workout_manager_user.controllers;

import com.nagpal.shivam.workout_manager_user.dtos.response.ResponseWrapper;
import com.nagpal.shivam.workout_manager_user.exceptions.ResponseException;
import com.nagpal.shivam.workout_manager_user.exceptions.handlers.GlobalExceptionHandler;
import com.nagpal.shivam.workout_manager_user.services.JWTService;
import com.nagpal.shivam.workout_manager_user.services.OTPService;
import com.nagpal.shivam.workout_manager_user.utils.Constants;
import com.nagpal.shivam.workout_manager_user.utils.MessageConstants;
import com.nagpal.shivam.workout_manager_user.utils.RoutingConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;

public class OTPController {
    private final Router router;
    private final OTPService otpService;
    private final JWTService jwtService;

    public OTPController(Vertx vertx, Router mainRouter, OTPService otpService, JWTService jwtService) {
        this.router = Router.router(vertx);
        this.jwtService = jwtService;
        mainRouter.mountSubRouter(RoutingConstants.OTP, router);
        this.otpService = otpService;

        setupEndpoints();
    }

    private void setupEndpoints() {
        resendOTP();
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
                    jwtService.verifyOTPToken(otpToken)
                            .compose(otpService::resendOTP)
                            .onSuccess(otpResponseDTO -> routingContext.response()
                                    .setStatusCode(HttpResponseStatus.OK.code())
                                    .end(Json.encodePrettily(ResponseWrapper.success(otpResponseDTO))))
                            .onFailure(throwable -> GlobalExceptionHandler.handle(throwable,
                                    routingContext.response()));
                });
    }
}
