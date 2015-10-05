package com.github.mokkun.playground.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public final class Preferences {
    private static final String PREFERENCES_NAME = "lemonade_prefs";
    private static final String SENSOR_STEP_COUNT = "sensor_step_count";
    private static final String START_TIME = "start_time";

    private Preferences() {
        throw new AssertionError("Instantiation is not supported.");
    }

    public static void clearStoredValues(@NonNull Context context) {
        getSharedPreferences(context).edit().clear().commit();
    }

    public static long retrieveStoredStartTime(@NonNull Context context) {
        return getSharedPreferences(context).getLong(START_TIME, Integer.MIN_VALUE);
    }

    public static long retrieveSensorInitialStepCount(@NonNull Context context) {
        return getSharedPreferences(context).getLong(SENSOR_STEP_COUNT, Integer.MIN_VALUE);
    }

    public static void storeSensorInitialStepCount(@NonNull Context context, long steps) {
        getSharedPreferences(context).edit().putLong(SENSOR_STEP_COUNT, steps).commit();
    }

    public static void storeStartTime(@NonNull Context context, long sinceWhen) {
        getSharedPreferences(context).edit().putLong(START_TIME, sinceWhen).commit();
    }

    private static SharedPreferences getSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}
