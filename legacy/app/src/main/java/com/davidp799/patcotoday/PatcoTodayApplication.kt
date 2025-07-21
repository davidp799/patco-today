package com.davidp799.patcotoday

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

class PatcoTodayApplication: Application() {
    override fun onCreate() {
        val sharedPreferences =
            getSharedPreferences("com.davidp799.patcotoday_preferences", Context.MODE_PRIVATE)

        when (sharedPreferences.getString("device_theme", "")) {
            "1" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            "2" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        if (sharedPreferences.getBoolean("dynamic_colors", false)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        super.onCreate()
    }
}