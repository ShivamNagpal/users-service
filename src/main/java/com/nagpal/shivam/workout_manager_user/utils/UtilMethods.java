package com.nagpal.shivam.workout_manager_user.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class UtilMethods {

    private UtilMethods() {
    }

    public static Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
