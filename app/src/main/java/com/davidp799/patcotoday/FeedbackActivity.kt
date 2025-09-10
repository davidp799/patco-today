package com.davidp799.patcotoday

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.preference.PreferenceManager
import com.davidp799.patcotoday.ui.screens.FeedbackScreen
import com.davidp799.patcotoday.ui.theme.PatcoTodayTheme

class FeedbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get theme preferences to configure system bars
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = prefs.getString("device_theme", "3")?.toInt() ?: 3
        val isSystemInDarkTheme = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

        // Determine if we should use dark theme
        val useDarkTheme = when (themePreference) {
            1 -> false // Light theme
            2 -> true  // Dark theme
            3 -> isSystemInDarkTheme // Follow system
            else -> isSystemInDarkTheme
        }

        // Configure edge-to-edge with proper system bar styles
        enableEdgeToEdge(
            statusBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            },
            navigationBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            }
        )

        setContent {
            PatcoTodayTheme {
                FeedbackScreen(
                    onNavigateUp = {
                        finish() // Simply finish this activity to go "back"
                    }
                )
            }
        }
    }
}
