package com.davidp799.patcotoday

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

class PatcoTodayApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        val sharedPreferences =
            getSharedPreferences("com.davidp799.patcotoday_preferences", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("dynamic_colors", false)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}