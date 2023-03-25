package com.davidp799.patcotoday

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors

class PatcoTodayApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // shared preferences
        val sharedPreferences = getSharedPreferences("com.davidp799.patcotoday_preferences", Context.MODE_PRIVATE)
        val isDynamic = sharedPreferences.getBoolean("dynamic_colors", false)
        if (isDynamic) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}