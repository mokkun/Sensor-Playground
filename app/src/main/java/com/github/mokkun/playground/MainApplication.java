package com.github.mokkun.playground;

import android.app.Application;

import com.github.mokkun.playground.database.PlaygroundContentProvider;
import com.github.mokkun.playground.service.SensorService;

public class MainApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        PlaygroundContentProvider.initialise();
        SensorService.startService(this);
    }
}
