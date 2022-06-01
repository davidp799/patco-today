package com.davidp799.patcotoday;

import android.app.Application;

import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.color.DynamicColors;

public class PatcoTodayApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Apply dynamic color
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
