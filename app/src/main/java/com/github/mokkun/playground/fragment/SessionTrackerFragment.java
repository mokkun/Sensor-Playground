package com.github.mokkun.playground.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.mokkun.playground.R;
import com.github.mokkun.playground.utils.DistanceUtils;
import com.github.mokkun.playground.view.widget.CustomToggleButton;

import java.util.Locale;

import static com.github.mokkun.playground.service.SensorService.*;

public class SessionTrackerFragment extends Fragment {
    private static final int INITIAL_STEPS_VALUE = 0;

    private TextView mActivityTypeTextView;
    private Chronometer mActivityChronometer;
    private String mDistanceFormat;
    private TextView mDistanceTextView;
    private Listener mListener;
    private String mStepsFormat;
    private TextView mStepsTextView;
    private CustomToggleButton mTrackToggleButton;

    public static Fragment createFragment() {
        return new SessionTrackerFragment();
    }

    public static CharSequence getTitle(@NonNull Context context) {
        return context.getString(R.string.title_session_tracker);
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (Listener) context;
        } catch (ClassCastException ignore) {
            throw new ClassCastException(context.toString() + " must implement Listener.");
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session_track, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mActivityTypeTextView = (TextView) view.findViewById(R.id.activity_type);
        updateActivityType(SensorInfo.ActivityType.STILL);

        mActivityChronometer = (Chronometer) view.findViewById(R.id.activity_time_text);
        mActivityChronometer.setFormat(getString(R.string.activity_time));
        mActivityChronometer.setBase(SystemClock.elapsedRealtime());

        mDistanceFormat = getString(R.string.distance);
        mDistanceTextView = (TextView) view.findViewById(R.id.activity_distance);
        mStepsFormat = getString(R.string.steps_count);
        mStepsTextView = (TextView) view.findViewById(R.id.activity_steps);
        updateSteps(INITIAL_STEPS_VALUE);



        mTrackToggleButton = (CustomToggleButton) view.findViewById(R.id.track_activity_btn);
        mTrackToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mListener.onTrackActivityStart();
                } else {
                    mListener.onTrackActivityStop();
                }
            }
        });
    }

    public void setTrackActivityStartTime(long sinceWhen) {
        if (mActivityChronometer != null) {
            mActivityChronometer.setBase(sinceWhen);
            mActivityChronometer.start();
        }
        if (mTrackToggleButton != null) {
            mTrackToggleButton.setCheckedState(true);
        }
    }

    public void setTrackActivityStop() {
        if (mActivityChronometer != null) {
            mActivityChronometer.stop();
            mActivityChronometer.setBase(SystemClock.elapsedRealtime());
        }
        if (mTrackToggleButton != null) {
            mTrackToggleButton.setCheckedState(false);
        }
        updateSteps(INITIAL_STEPS_VALUE);
        updateActivityType(SensorInfo.ActivityType.STILL);
    }

    public void updateInfo(@NonNull SensorInfo info) {
        updateSteps(info.getSteps());
        updateActivityType(info.getActivityType());
    }


    private void updateActivityType(@NonNull SensorInfo.ActivityType type) {
        if (mActivityTypeTextView != null) {
            mActivityTypeTextView.setText(getString(type.getStringResId()));
        }
    }

    private void updateSteps(long steps) {
        if (mDistanceFormat != null && mDistanceTextView != null) {
            final double distance = DistanceUtils.distanceFromSteps(steps);
            mDistanceTextView.setText(String.format(Locale.ENGLISH, mDistanceFormat, distance));
        }
        if (mStepsFormat != null && mStepsTextView != null) {
            mStepsTextView.setText(String.format(Locale.ENGLISH, mStepsFormat, steps));
        }
    }

    public interface Listener {
        void onTrackActivityStart();
        void onTrackActivityStop();
    }
}
