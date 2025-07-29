package com.davidp799.patcotoday.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.preference.PreferenceManager

object NetworkUtils {

    /**
     * Checks if the device is currently connected to a mobile data network
     */
    fun isOnMobileData(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /**
     * Gets the user's preference for downloading on mobile data
     */
    fun isDownloadOnMobileDataEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean("download_on_mobile_data", true)
    }

    /**
     * Checks if network operations should be allowed based on connection type and user preferences
     * Returns true if operations are allowed, false if blocked
     */
    fun shouldAllowNetworkOperation(context: Context): Boolean {
        val isOnMobile = isOnMobileData(context)
        val allowMobileData = isDownloadOnMobileDataEnabled(context)

        // Allow if not on mobile data, or if on mobile data and user allows it
        return !isOnMobile || allowMobileData
    }

    /**
     * Shows a toast message when network operation is blocked due to mobile data restrictions
     */
    fun showMobileDataBlockedToast(context: Context) {
        Toast.makeText(
            context,
            "Data-saver is enabled. Working offline.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
