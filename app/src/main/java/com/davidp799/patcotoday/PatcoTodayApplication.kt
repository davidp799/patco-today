package com.davidp799.patcotoday

import android.app.Application
import com.google.android.material.color.DynamicColors

class PatcoTodayApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply dynamic color
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}