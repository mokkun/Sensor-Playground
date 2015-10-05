package com.github.mokkun.playground.utils;

import android.util.Log;

import com.github.mokkun.playground.BuildConfig;

import java.util.Date;

@SuppressWarnings("unused")
public final class Logger {

    private Logger() {
        throw new AssertionError("Instantiation is not supported.");
    }

    private static final boolean LOGGING_ENABLED = BuildConfig.DEBUG;
    private static final String COMMON_LOGGING_TAG = "mokkun";
    private static final String MESSAGE_FORMAT = "%s | %s : %s";

    public static void v(Object caller, String message) {
        if (LOGGING_ENABLED && Log.isLoggable(COMMON_LOGGING_TAG, Log.VERBOSE)) {
            Log.v(COMMON_LOGGING_TAG,
                    String.format(MESSAGE_FORMAT, getCurrentTimeString(), getClassName(caller),
                            message));
        }
    }

    public static void d(Object caller, String message) {
        if (LOGGING_ENABLED && Log.isLoggable(COMMON_LOGGING_TAG, Log.DEBUG)) {
            Log.d(COMMON_LOGGING_TAG,
                    String.format(MESSAGE_FORMAT, getCurrentTimeString(), getClassName(caller),
                            message));
        }
    }

    public static void i(Object caller, String message) {
        if (LOGGING_ENABLED && Log.isLoggable(COMMON_LOGGING_TAG, Log.INFO)) {
            Log.i(COMMON_LOGGING_TAG,
                    String.format(MESSAGE_FORMAT, getCurrentTimeString(), getClassName(caller),
                            message));
        }
    }

    public static void w(Object caller, String message) {
        if (LOGGING_ENABLED && Log.isLoggable(COMMON_LOGGING_TAG, Log.WARN)) {
            Log.w(COMMON_LOGGING_TAG,
                    String.format(MESSAGE_FORMAT, getCurrentTimeString(), getClassName(caller),
                            message));
        }
    }

    public static void e(Object caller, String message) {
        Logger.e(caller, message, null);
    }

    @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
    public static void e(Object caller, String message, Throwable error) {
        if (LOGGING_ENABLED && Log.isLoggable(COMMON_LOGGING_TAG, Log.ERROR)) {
            if (error != null) {
                Log.e(COMMON_LOGGING_TAG,
                        String.format(MESSAGE_FORMAT, getCurrentTimeString(), getClassName(caller),
                                message), error);
            } else {
                Log.e(COMMON_LOGGING_TAG,
                        String.format(MESSAGE_FORMAT, getCurrentTimeString(), getClassName(caller),
                                message));
            }
        }
    }

    private static String getCurrentTimeString() {
        return DateFormatUtils.formatTime(new Date(System.currentTimeMillis()));
    }

    private static String getClassName(Object caller) {
        String className = caller.getClass().getName();
        int firstChar = className.lastIndexOf('.') + 1;
        if (firstChar > 0) {
            className = className.substring(firstChar);
        }
        return className;
    }
}
