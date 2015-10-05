package com.github.mokkun.playground.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.github.mokkun.playground.R;
import com.github.mokkun.playground.fragment.SessionHistoryFragment;
import com.github.mokkun.playground.fragment.SessionTrackerFragment;
import com.github.mokkun.playground.service.SensorService;

import static com.github.mokkun.playground.database.PlaygroundContentProvider.Content;
import static com.github.mokkun.playground.database.PlaygroundContentProvider.Service;
import static com.github.mokkun.playground.database.PlaygroundContentProvider.matchUri;

public class MainActivity extends AppCompatActivity implements ServiceConnection,
        SensorService.SensorListener, SessionTrackerFragment.Listener{
    private SensorService.SensorBinder mBinder;
    private SessionTrackerFragment mTrackerFragment;
    private SessionHistoryFragment mHistoryFragment;
    private final ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        @Override public void onChange(boolean selfChange, Uri uri) {
            switch (matchUri(uri)) {
                case Content.SESSIONS:
                        refreshSessionHistoryFragment();
                    break;
                default:
            }
        }
    };

    //----------------------------------------
    // Activity Lifecycle
    //----------------------------------------

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SectionsPagerAdapter sectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override protected void onStart() {
        super.onStart();
        getContentResolver().registerContentObserver(Service.SESSIONS.getUri(), true,
                mContentObserver);
    }

    @Override protected void onResume() {
        super.onResume();
        bindService(new Intent(this, SensorService.class), this, BIND_AUTO_CREATE);
    }

    @Override protected void onPause() {
        super.onPause();
        if (mBinder != null) {
            mBinder = null;
            unbindService(this);
        }
    }

    @Override protected void onStop() {
        super.onStop();
        getContentResolver().unregisterContentObserver(mContentObserver);
    }

    //----------------------------------------
    // Sensors
    //----------------------------------------

    @Override public void onSensorStart(long sinceWhen) {
        startTrackActivity(sinceWhen);
    }

    @Override public void onSensorUpdate(@NonNull SensorService.SensorInfo info) {
        if (mTrackerFragment != null) {
            mTrackerFragment.updateInfo(info);
        }
    }

    @Override public void onSensorStop() {
        stopTrackActivity();
    }

    //----------------------------------------
    // Service
    //----------------------------------------

    @Override public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (SensorService.SensorBinder) service;
        mBinder.setListener(this);
        if (mBinder.isNotTracking()) {
            stopTrackActivity();
        } else {
            startTrackActivity(mBinder.getTrackingStartTime());
        }
    }

    @Override public void onServiceDisconnected(ComponentName name) {
        mBinder = null;
    }

    //----------------------------------------
    // Activity Tracking
    //----------------------------------------

    @Override public void onTrackActivityStart() {
        if (mBinder != null) {
            mBinder.startTracking();
        }
    }

    @Override public void onTrackActivityStop() {
        if (mBinder != null) {
            mBinder.stopTracking();
        }
    }

    private void startTrackActivity(long sinceWhen) {
        if (mTrackerFragment != null) {
            mTrackerFragment.setTrackActivityStartTime(sinceWhen);
        }
    }

    private void stopTrackActivity() {
        if (mTrackerFragment != null) {
            mTrackerFragment.setTrackActivityStop();
        }
    }

    //----------------------------------------
    // History
    //----------------------------------------

    private void refreshSessionHistoryFragment() {
        if (mHistoryFragment != null) {
            mHistoryFragment.refresh();
        }
    }


    //----------------------------------------
    // Sections Pages
    //----------------------------------------

    private static class Sections {
        public static final int TRACKER = 0;
        public static final int HISTORY = 1;
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private static final int SECTION_COUNT = 2;

        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override public Object instantiateItem(ViewGroup container, int position) {
            final Object item = super.instantiateItem(container, position);
            switch (position) {
                case Sections.TRACKER:
                    mTrackerFragment = (SessionTrackerFragment) item;
                    break;
                case Sections.HISTORY:
                    mHistoryFragment = (SessionHistoryFragment) item;
                    break;
                default:
            }
            return item;
        }

        @Override public Fragment getItem(int position) {
            switch (position) {
                case Sections.TRACKER:
                    return SessionTrackerFragment.createFragment();
                case Sections.HISTORY:
                    return SessionHistoryFragment.createFragment();
            }
            throw new RuntimeException("Invalid fragment adapter position.");
        }

        @Override public int getCount() {
            return SECTION_COUNT;
        }

        @Override public CharSequence getPageTitle(int position) {
            switch (position) {
                case Sections.TRACKER:
                    return SessionTrackerFragment.getTitle(MainActivity.this);
                case Sections.HISTORY:
                    return SessionHistoryFragment.getTitle(MainActivity.this);
            }
            return null;
        }
    }
}
