package com.davidp799.patcotoday.utils

import android.content.Context
import android.content.SharedPreferences

object VersionCodeStore {
    private const val PREFS_NAME = "settings"
    private const val VERSION_CODE_KEY = "version_code"

    fun getVersionCode(context: Context): Int {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(VERSION_CODE_KEY, -1)
    }

    fun setVersionCode(context: Context, versionCode: Int) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(VERSION_CODE_KEY, versionCode).apply()
    }
}
