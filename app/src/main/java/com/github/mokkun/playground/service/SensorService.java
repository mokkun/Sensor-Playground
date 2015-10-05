package com.github.mokkun.playground.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.github.mokkun.playground.R;
import com.github.mokkun.playground.database.PlaygroundContentProvider;
import com.github.mokkun.playground.filters.ScalarKalmanFilter;
import com.github.mokkun.playground.utils.DateFormatUtils;
import com.github.mokkun.playground.utils.DistanceUtils;
import com.github.mokkun.playground.utils.Logger;
import com.github.mokkun.playground.utils.Preferences;

import java.util.Date;

import static com.github.mokkun.playground.database.PlaygroundContract.SessionEntry;

public class SensorService extends Service implements SensorEventListener {
    private static final int NOTIFICATION_ID = 42;
    private static final int NOTIFICATION_CLICK = 43;
    private static final int WALKING_THRESHOLD = 2;
    private static final int RUNNING_THRESHOLD = 9;

    private final SensorBinder mBinder = new LocalBinder();
    private final ScalarKalmanFilter[] mKalmanFilters = new ScalarKalmanFilter[3];
    private SensorListener mListener = null;
    private SensorInfo mSensorInfo = null;
    private long mSessionInitialSteps = Integer.MIN_VALUE;
    private long mSessionStartTimeMillis = Integer.MIN_VALUE;

    public static void startService(@NonNull Context context) {
        context.startService(new Intent(context, SensorService.class));
    }

    //----------------------------------------
    // Lifecycle Events
    //----------------------------------------

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && Notification.STOP_SERVICE.equals(intent.getAction())) {
            Logger.d(this, "Stopping service.");
            mBinder.stopTracking();
        } else {
            Logger.d(this, "onStartCommand");
            restoreServiceState();
        }
        return START_STICKY;
    }

    @Override public void onDestroy() {
        Logger.d(this, "onDestroy");
        Preferences.clearStoredValues(this);
    }

    @Nullable @Override public IBinder onBind(Intent intent) {
        Logger.d(this, "onBind");
        cancelCurrentNotification();
        return (IBinder) mBinder;
    }

    @Override public void onRebind(Intent intent) {
        Logger.d(this, "onRebind");
        cancelCurrentNotification();
    }

    @Override public boolean onUnbind(Intent intent) {
        Logger.d(this, "onUnbind");
        mListener = null;
        if (isNotRunning()) {
            stopSelf();
        } else {
            displayNotification();
        }
        return true;
    }

    //----------------------------------------
    // Sensors
    //----------------------------------------

    @Override public void onSensorChanged(@NonNull SensorEvent event) {
        if (mSensorInfo == null) {
            return;
        }
        switch (event.sensor.getType()) {
            case Sensor.TYPE_STEP_COUNTER:
                final long totalSteps = (long) event.values[0];
                if (mSessionInitialSteps == Integer.MIN_VALUE) {
                    mSessionInitialSteps = totalSteps;
                    Preferences.storeSensorInitialStepCount(this, mSessionInitialSteps);
                }
                mSensorInfo.setSteps(totalSteps - mSessionInitialSteps);
                Logger.d(this, "Step count: " + mSensorInfo.getSteps());
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                final float x = event.values[0];
                final float y = event.values[1];
                final float z = event.values[2];
                //noinspection SuspiciousNameCombination
                final double totalAcc = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
                float filteredAcc = filterAcceleration((float) totalAcc);
                SensorInfo.ActivityType activityType = SensorInfo.ActivityType.STILL;
                if (filteredAcc < WALKING_THRESHOLD) {
                    Logger.d(this, "Still: " + filteredAcc);
                    activityType = SensorInfo.ActivityType.STILL;
                } else if (filteredAcc > WALKING_THRESHOLD && filteredAcc < RUNNING_THRESHOLD) {
                    Logger.d(this, "Walking: " + filteredAcc);
                    activityType = SensorInfo.ActivityType.WALKING;
                } else if (filteredAcc > RUNNING_THRESHOLD) {
                    Logger.d(this, "Running: " + filteredAcc);
                    activityType = SensorInfo.ActivityType.RUNNING;
                }
                if (!activityType.equals(mSensorInfo.getActivityType())) {
                    mSensorInfo.setActivityType(activityType);
                }
                break;
            default:
        }
        if (mListener != null) {
            mListener.onSensorUpdate(mSensorInfo);
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private SensorManager getSensorManager() {
        return (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    private float filterAcceleration(float value) {
        float filter1 = mKalmanFilters[0].filter(value);
        float filter2 = mKalmanFilters[1].filter(filter1);
        return mKalmanFilters[2].filter(filter2);
    }

    private void registerSensors() {
        final SensorManager sensorManager = getSensorManager();
        if (sensorManager != null) {
            mSensorInfo = new SensorInfo();
            final Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepSensor != null) {
                mSessionInitialSteps = Preferences.retrieveSensorInitialStepCount(this);
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
            final Sensor linearAccelerationSensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            if (linearAccelerationSensor != null) {
                // Init filters.
                mKalmanFilters[0] = new ScalarKalmanFilter();
                mKalmanFilters[1] = new ScalarKalmanFilter();
                mKalmanFilters[2] = new ScalarKalmanFilter();
                sensorManager.registerListener(this, linearAccelerationSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private void startSensors() {
        Logger.d(this, "startSensors");
        // Clock.
        mSessionStartTimeMillis = SystemClock.elapsedRealtime();
        Preferences.storeStartTime(this, mSessionStartTimeMillis);
        // Sensors.
        registerSensors();
        if (mListener != null) {
            mListener.onSensorStart(mSessionStartTimeMillis);
        }
    }

    private void stopSensors() {
        Logger.d(this, "stopSensors");
        // Store data.
        saveSessionData();
        // Clear state.
        Preferences.clearStoredValues(this);
        mSessionStartTimeMillis = Integer.MIN_VALUE;
        mSessionInitialSteps = Integer.MIN_VALUE;
        mSensorInfo = null;
        // Sensors.
        unregisterSensors();
        if (mListener != null) {
            mListener.onSensorStop();
        }
    }

    private void unregisterSensors() {
        Logger.d(this, "unregisterSensors");
        final SensorManager sensorManager = getSensorManager();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    //----------------------------------------
    // Session Data
    //----------------------------------------

    private void saveSessionData() {
        Logger.d(this, "saveSessionData");

        Logger.d(this, "Start time: " + DateFormatUtils.formatDateTimeNoYear(
                new Date(getSessionStartTime())));
        Logger.d(this, "Duration: " + DateFormatUtils.formatTimeNoZone(
                new Date(getSessionDuration())));
        Logger.d(this, "Steps: + " + mSensorInfo.getSteps());
        Logger.d(this, "Distance: " + DistanceUtils.distanceFromSteps(mSensorInfo.getSteps()));

        final ContentValues contentValues = new ContentValues();
        contentValues.put(SessionEntry.COLUMN_NAME_START_TIME, getSessionStartTime());
        contentValues.put(SessionEntry.COLUMN_NAME_DURATION, getSessionDuration());
        contentValues.put(SessionEntry.COLUMN_NAME_STEPS, mSensorInfo.getSteps());
        contentValues.put(SessionEntry.COLUMN_NAME_DISTANCE,
                DistanceUtils.distanceFromSteps(mSensorInfo.getSteps()));
        getContentResolver().insert(PlaygroundContentProvider.Service.SESSIONS.getUri(),
                contentValues);
    }

    private long getSessionStartTime() {
        return System.currentTimeMillis() - getSessionDuration();
    }

    private long getSessionDuration() {
        return SystemClock.elapsedRealtime() - mSessionStartTimeMillis;
    }

    //----------------------------------------
    // Service State
    //----------------------------------------

    private boolean isNotRunning() {
        return mSessionStartTimeMillis == Integer.MIN_VALUE;
    }

    private void restoreServiceState() {
        mSessionStartTimeMillis = Preferences.retrieveStoredStartTime(this);
        mSessionInitialSteps = Preferences.retrieveSensorInitialStepCount(this);
    }

    //----------------------------------------
    // System Notifications
    //----------------------------------------

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private PendingIntent createNotificationContentIntent() {
        return PendingIntent.getService(this, NOTIFICATION_CLICK,
                new Intent(this, SensorService.class).setAction(Notification.STOP_SERVICE), 0);
    }

    private void cancelCurrentNotification() {
        final NotificationManager notificationManager = getNotificationManager();
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private android.app.Notification createNotification(long sinceWhen) {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_clock)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tap_to_stop_tracking))
                .setContentIntent(createNotificationContentIntent())
                .setWhen(System.currentTimeMillis() - (SystemClock.elapsedRealtime() - sinceWhen))
                .setUsesChronometer(true)
                .setOngoing(true)
                .build();
    }

    private void displayNotification() {
        final NotificationManager notificationManager = getNotificationManager();
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification(mSessionStartTimeMillis));
        }
    }

    //----------------------------------------
    // Local Binder
    //----------------------------------------

    private class LocalBinder extends Binder implements SensorBinder {
        @Override public long getTrackingStartTime() {
            return mSessionStartTimeMillis;
        }

        @Override public boolean isNotTracking() {
            return isNotRunning();
        }

        @Override public void setListener(@NonNull SensorListener listener) {
            mListener = listener;
            if (!isNotTracking()) {
                mListener.onSensorStart(mSessionStartTimeMillis);
                registerSensors();
            }
        }

        @Override public void startTracking() {
            Logger.d(this, "startTracking");
            if (isNotRunning()) {
                SensorService.startService(SensorService.this);
            }
            startSensors();
        }

        @Override public void stopTracking() {
            Logger.d(this, "stopTracking");
            cancelCurrentNotification();
            stopSensors();
            stopSelf();
        }
    }

    //----------------------------------------
    // Interfaces
    //----------------------------------------

    public interface SensorListener {
        void onSensorStart(long sinceWhen);
        void onSensorUpdate(@NonNull SensorInfo info);
        void onSensorStop();
    }

    public interface SensorBinder {
        void setListener(@NonNull SensorListener listener);
        long getTrackingStartTime();
        boolean isNotTracking();
        void startTracking();
        void stopTracking();
    }

    //----------------------------------------
    // Internal Classes
    //----------------------------------------

    public static class SensorInfo {
        private long mSteps = 0;
        private ActivityType mActivityType = ActivityType.STILL;

        public ActivityType getActivityType() {
            return mActivityType;
        }

        public long getSteps() {
            return mSteps;
        }

        void setActivityType(ActivityType type) {
            mActivityType = type;
        }

        void setSteps(long steps) {
            mSteps = steps;
        }

        public enum ActivityType {
            STILL(R.string.still),
            WALKING(R.string.walking),
            RUNNING(R.string.running);

            private final int mResId;

            ActivityType(int resId) {
                mResId = resId;
            }

            public int getStringResId() {
                return mResId;
            }
        }
    }

    private static class Notification {
        public static final String STOP_SERVICE =
                "com.github.mokkun.android.service.sensor.stop";
    }
}
