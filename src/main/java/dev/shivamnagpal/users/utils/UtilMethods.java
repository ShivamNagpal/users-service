package dev.shivamnagpal.users.utils;

import io.vertx.core.Future;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class UtilMethods {

    private UtilMethods() {
    }

    public static Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static <T> Future<T> nonBlockingWhile(Supplier<Future<T>> supplier, Predicate<T> predicate) {
        return supplier.get()
                .compose(result -> {
                    if (predicate.test(result)) {
                        return Future.succeededFuture(result);
                    }
                    return nonBlockingWhile(supplier, predicate);
                });
    }
}
