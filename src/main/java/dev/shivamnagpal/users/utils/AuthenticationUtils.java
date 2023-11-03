package dev.shivamnagpal.users.utils;

import dev.shivamnagpal.users.exceptions.ResponseException;
import dev.shivamnagpal.users.services.JWTService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;

public class AuthenticationUtils {

    private AuthenticationUtils() {
    }

    public static Future<Void> authenticate(HttpServerRequest request, JWTService jwtService) {
        String path = request.path();
        if (RoutingConstants.PUBLIC_ROUTES.contains(path)) {
            return Future.succeededFuture();
        }
        String authorizationValue = request.getHeader(Constants.AUTHORIZATION);
        if (authorizationValue == null) {
            return Future.failedFuture(
                    new ResponseException(
                            HttpResponseStatus.BAD_REQUEST.code(),
                            MessageConstants.AUTHORIZATION_HEADER_NOT_PROVIDED, null
                    )
            );
        }
        if (!authorizationValue.startsWith(Constants.BEARER_SPACE)) {
            return Future.failedFuture(
                    new ResponseException(
                            HttpResponseStatus.BAD_REQUEST.code(),
                            MessageConstants.AUTHORIZATION_TOKEN_MUST_START_WITH_THE_BEARER, null
                    )
            );
        }
        String authToken = authorizationValue.substring(7);
        return jwtService.verifyAuthToken(authToken);
    }

    public static String getAuthToken(HttpServerRequest request) {
        return request.getHeader(Constants.AUTHORIZATION).substring(7);
    }
}
