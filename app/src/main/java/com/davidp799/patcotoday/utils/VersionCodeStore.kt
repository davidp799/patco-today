package com.davidp799.patcotoday.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.core.content.edit

object VersionCodeStore {
    private const val VERSION_CODE_KEY = "version_code"

    fun getVersionCode(context: Context): Int {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(VERSION_CODE_KEY, -1)
    }

    fun setVersionCode(context: Context, versionCode: Int) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit { putInt(VERSION_CODE_KEY, versionCode) }
    }
}
