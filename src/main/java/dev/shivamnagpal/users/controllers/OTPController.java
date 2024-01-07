package dev.shivamnagpal.users.controllers;

import dev.shivamnagpal.users.core.Controller;
import dev.shivamnagpal.users.core.RequestPath;
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
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class OTPController extends Controller {
    private final JsonObject config;

    private final OTPService otpService;

    private final JWTService jwtService;

    public OTPController(
            Router router,
            RequestPath requestPath,
            JsonObject config,
            OTPService otpService,
            JWTService jwtService
    ) {
        super(router, requestPath.next(RoutingConstants.OTP));
        this.config = config;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    @Override
    public void registerRoutes() {
        resendOTP();
        verifyOTP();
    }

    private void resendOTP() {
        super.router.post(super.requestPath.next(RoutingConstants.RESEND_OTP).path())
                .handler(routingContext -> {
                    String otpToken = routingContext.request().getHeader(Constants.OTP_TOKEN);
                    if (otpToken == null) {
                        ResponseException exception = new ResponseException(
                                HttpResponseStatus.BAD_REQUEST.code(),
                                MessageConstants.OTP_TOKEN_NOT_PROVIDED,
                                null
                        );
                        GlobalExceptionHandler.handle(exception, routingContext.response());
                        return;
                    }
                    jwtService.verifyAndDecodeOTPToken(otpToken)
                            .compose(otpService::resendOTP)
                            .onSuccess(
                                    otpResponseDTO -> routingContext.response()
                                            .setStatusCode(HttpResponseStatus.OK.code())
                                            .end(Json.encodePrettily(ResponseWrapper.success(otpResponseDTO)))
                            )
                            .onFailure(
                                    throwable -> GlobalExceptionHandler.handle(
                                            throwable,
                                            routingContext.response()
                                    )
                            );
                });
    }

    private void verifyOTP() {
        super.router.post(super.requestPath.next(RoutingConstants.VERIFY_OTP).path())
                .handler(routingContext -> {
                    String otpToken = routingContext.request().getHeader(Constants.OTP_TOKEN);
                    if (otpToken == null) {
                        ResponseException exception = new ResponseException(
                                HttpResponseStatus.BAD_REQUEST.code(),
                                MessageConstants.OTP_TOKEN_NOT_PROVIDED,
                                null
                        );
                        GlobalExceptionHandler.handle(exception, routingContext.response());
                        return;
                    }
                    jwtService.verifyAndDecodeOTPToken(otpToken)
                            .compose(
                                    jWTOTPTokenDTO -> RequestValidationUtils.fetchBodyAsJson(routingContext)
                                            .compose(
                                                    body -> VerifyOTPRequestDTO.fromRequest(
                                                            body,
                                                            config,
                                                            jWTOTPTokenDTO.getOtpPurpose()
                                                    )
                                            )
                                            .compose(
                                                    verifyOTPRequestDTO -> otpService.verifyOTP(
                                                            jWTOTPTokenDTO,
                                                            verifyOTPRequestDTO
                                                    )
                                            )
                            )
                            .onSuccess(
                                    obj -> routingContext.response()
                                            .setStatusCode(HttpResponseStatus.OK.code())
                                            .end(Json.encodePrettily(ResponseWrapper.success(obj)))
                            )
                            .onFailure(
                                    throwable -> GlobalExceptionHandler.handle(
                                            throwable,
                                            routingContext.response()
                                    )
                            );
                });
    }
}
