package com.nagpal.shivam.workout_manager_user.utils;

import java.util.Set;

public class RoutingConstants {

    public static final String USER = "/user";
    public static final String SIGN_UP = "/sign-up";
    public static final String OTP = "/otp";
    public static final String RESEND_OTP = "/resend-otp";
    public static final String VERIFY_OTP = "/verify-otp";
    public static final String LOGIN = "/login";
    public static final String SESSION = "/session";
    public static final String REFRESH_TOKEN = "/refresh-token";
    public static final String HEALTH = "/health";
    public static final String PATH_SEPARATOR = "/";
    public static final String DB = "/db";
    public static final String LOGOUT = "/logout";

    public static final Set<String> PUBLIC_ROUTES = Set.of(
            HEALTH,
            HEALTH + DB,
            USER + SIGN_UP,
            USER + LOGIN,
            OTP + RESEND_OTP,
            OTP + VERIFY_OTP,
            SESSION + REFRESH_TOKEN
    );

    private RoutingConstants() {
    }
}
