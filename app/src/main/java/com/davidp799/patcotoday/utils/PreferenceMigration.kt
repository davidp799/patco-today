package com.davidp799.patcotoday.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.core.content.edit

object PreferenceMigration {
    private const val TAG = "PreferenceMigration"
    private const val MIGRATION_COMPLETED_KEY = "migration_2_0_completed"

    /**
     * Migrates preferences from version 1.x.x to version 2.0
     * - Moves version_code from separate "settings" file back to default preferences
     * - Preserves user settings during upgrade
     */
    fun migratePreferencesIfNeeded(context: Context) {
        val defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        // Check if migration has already been completed
        if (defaultPrefs.getBoolean(MIGRATION_COMPLETED_KEY, false)) {
            Log.d(TAG, "Migration already completed, skipping")
            return
        }

        Log.d(TAG, "Starting preference migration for version 2.0")

        // Get the separate settings file that was introduced in 2.0
        val settingsPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

        // Check if there's a version_code in the separate settings file
        val versionCodeFromSettings = settingsPrefs.getInt("version_code", -1)
        val versionCodeFromDefaults = defaultPrefs.getInt("version_code", -1)

        defaultPrefs.edit {

            // If version_code exists in settings file but not in default prefs, move it
            if (versionCodeFromSettings != -1 && versionCodeFromDefaults == -1) {
                Log.d(
                    TAG,
                    "Moving version_code from settings file to default preferences: $versionCodeFromSettings"
                )
                putInt("version_code", versionCodeFromSettings)

                // Remove from settings file since we're moving it back
                settingsPrefs.edit { remove("version_code") }
            }

            // Ensure default values are set for any missing preferences that might have been lost
            migrateMissingDefaults(defaultPrefs, this)

            // Mark migration as completed
            putBoolean(MIGRATION_COMPLETED_KEY, true)
        }

        Log.d(TAG, "Preference migration completed successfully")
    }

    /**
     * Ensures default values are set for preferences that might be missing
     */
    private fun migrateMissingDefaults(prefs: SharedPreferences, editor: SharedPreferences.Editor) {
        // Set default values for preferences if they don't exist
        if (!prefs.contains("device_theme")) {
            editor.putString("device_theme", "3") // Follow system as default
            Log.d(TAG, "Setting default device_theme: 3")
        }

        if (!prefs.contains("dynamic_colors")) {
            editor.putBoolean("dynamic_colors", false) // Default to false for compatibility
            Log.d(TAG, "Setting default dynamic_colors: false")
        }

        if (!prefs.contains("download_on_mobile_data")) {
            editor.putBoolean("download_on_mobile_data", true) // Default to true
            Log.d(TAG, "Setting default download_on_mobile_data: true")
        }

        if (!prefs.contains("visit_number")) {
            editor.putInt("visit_number", 0) // Default to 0
            Log.d(TAG, "Setting default visit_number: 0")
        }
    }
}
