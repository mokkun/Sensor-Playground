package com.github.mokkun.playground.filters;

// http://www.swarthmore.edu/NatSci/echeeve1/Ref/Kalman/ScalarKalman.html
public final class ScalarKalmanFilter {
    private static final float DEFAULT_COVARIANCE = 0.1f;
    private static final float DEFAULT_STATE = 0f;
    private static final float DEFAULT_STATE_TRANSITION = 1f;
    private static final float DEFAULT_MEASUREMENT = 1f;
    private static final float DEFAULT_PROCESS_NOISE_COVARIANCE = 0.01f;
    private static final float DEFAULT_MEASUREMENT_NOISE_COVARIANCE = 0.025f;

    private float mX = DEFAULT_STATE;
    private float mP = DEFAULT_COVARIANCE;

    public ScalarKalmanFilter() {}

    public float filter(float value) {
        // These fields are declared here just to make the code reading easier.
        final float a = DEFAULT_STATE_TRANSITION;
        final float h = DEFAULT_MEASUREMENT;
        final float r = DEFAULT_MEASUREMENT_NOISE_COVARIANCE;
        final float q = DEFAULT_PROCESS_NOISE_COVARIANCE;
        // Predictor step.
        mX *= a;
        mP = (a * a) * mP + q;
        // Kalman filter gain.
        final float k = h * mP / ((h * h) * mP + r);
        // Corrector step.
        mX = mX + k * (value - h * mX);
        mP *= (1 - (h * k));
        return mX;
    }
}
