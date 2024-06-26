package com.akrog.tolomet.utils;

import java.util.Calendar;

public final class DateUtils {
    private DateUtils() {}

    public static void resetDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public static void endDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
    }

    public static boolean isToday(long stamp) {
        Calendar today = Calendar.getInstance();
        resetDay(today);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(stamp);
        resetDay(cal);
        return cal.equals(today);
    }

    public static long today() {
        Calendar today = Calendar.getInstance();
        resetDay(today);
        return today.getTimeInMillis();
    }
}
