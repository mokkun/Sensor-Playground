package com.github.mokkun.playground.utils;

public final class DistanceUtils {
    // This is just to simplify the display data, user input is needed for a more accurate measure.
    // Here we assume that the user is a male with height of 1.70cm.
    private static final double AVERAGE_STEP_LENGTH = 0.415 * 1.70;
    private static final double KILOMETER_MULTIPLIER = 0.001;

    private DistanceUtils() {
        throw new AssertionError("Instantiation is not supported.");
    }

    public static double distanceFromSteps(long steps) {
        return (steps * AVERAGE_STEP_LENGTH) * KILOMETER_MULTIPLIER;
    }
}
