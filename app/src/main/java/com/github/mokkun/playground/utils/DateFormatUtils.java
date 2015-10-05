package com.github.mokkun.playground.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Thread-safe collection of date format utility methods.
 */
public final class DateFormatUtils {
    private static final ThreadLocal<DateFormat> TIME_FORMAT =
            new InheritableThreadLocal<DateFormat>() {
                @Override protected DateFormat initialValue() {
                    final SimpleDateFormat dateFormat =
                            new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return dateFormat;
                }

                @Override protected DateFormat childValue(DateFormat parentValue) {
                    return (DateFormat) parentValue.clone();
                }
            };
    private static final ThreadLocal<DateFormat> TIME_NO_ZONE_FORMAT =
            new InheritableThreadLocal<DateFormat>() {
                @Override protected DateFormat initialValue() {
                    final SimpleDateFormat dateFormat =
                            new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return dateFormat;
                }

                @Override protected DateFormat childValue(DateFormat parentValue) {
                    return (DateFormat) parentValue.clone();
                }
            };
    private static final ThreadLocal<DateFormat> DATE_TIME_NO_YEAR_FORMAT =
            new InheritableThreadLocal<DateFormat>() {
                @Override protected DateFormat initialValue() {
                    return new SimpleDateFormat("MM/dd HH:mm", Locale.ENGLISH);
                }

                @Override protected DateFormat childValue(DateFormat parentValue) {
                    return (DateFormat) parentValue.clone();
                }
            };

    private DateFormatUtils() {
        throw new AssertionError("Instantiation is not supported.");
    }

    public static String formatTime(Date date) {
        return TIME_FORMAT.get().format(date);
    }

    public static String formatTimeNoZone(Date date) {
        return TIME_NO_ZONE_FORMAT.get().format(date);
    }

    public static String formatDateTimeNoYear(Date date) {
        return DATE_TIME_NO_YEAR_FORMAT.get().format(date);
    }
}
