package com.davidp799.patcotoday.data.local

import android.content.Context
import android.util.Log
import com.davidp799.patcotoday.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FileManager(private val context: Context) {

    private val client = OkHttpClient()

    private fun getSchedulesDirectory(): File {
        val schedulesDir = File(context.filesDir, "schedules")
        if (!schedulesDir.exists()) {
            schedulesDir.mkdirs()
        }
        return schedulesDir
    }

    private fun getRegularSchedulesDirectory(): File {
        val regularDir = File(getSchedulesDirectory(), "regular")
        if (!regularDir.exists()) {
            regularDir.mkdirs()
        }
        return regularDir
    }

    private fun getSpecialSchedulesDirectory(date: String): File {
        val specialDir = File(getSchedulesDirectory(), "special")
        if (!specialDir.exists()) {
            specialDir.mkdirs()
        }
        val dateDir = File(specialDir, date)
        if (!dateDir.exists()) {
            dateDir.mkdirs()
        }
        return dateDir
    }

    suspend fun downloadAndSaveFile(url: String, fileName: String, isSpecial: Boolean = false, date: String? = null): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Check if network operations are allowed based on mobile data settings
                if (!NetworkUtils.shouldAllowNetworkOperation(context)) {
                    withContext(Dispatchers.Main) {
                        NetworkUtils.showMobileDataBlockedToast(context)
                    }
                    return@withContext false
                }

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val directory = if (isSpecial && date != null) {
                        getSpecialSchedulesDirectory(date)
                    } else {
                        getRegularSchedulesDirectory()
                    }

                    val file = File(directory, fileName)
                    val inputStream = response.body.byteStream()
                    val outputStream = FileOutputStream(file)

                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()

                    true
                } else {
                    Log.e("[downloadAndSaveFile]", "File download failed - Code: ${response.code}, URL: $url")
                    false
                }
            } catch (e: Exception) {
                Log.e("[downloadAndSaveFile]", "File download exception for $fileName: ${e.message}", e)
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun downloadAndSavePdf(url: String, fileName: String, date: String): Boolean {
        return downloadAndSaveFile(url, fileName, isSpecial = true, date = date)
    }

    fun getOldestLastModified(): String {
        val files = listOf(
            "weekdays_east.csv",
            "weekdays_west.csv",
            "saturdays_east.csv",
            "saturdays_west.csv",
            "sundays_east.csv",
            "sundays_west.csv"
        )

        var oldestTime = Long.MAX_VALUE
        files.forEach { fileName ->
            val file = File(getRegularSchedulesDirectory(), fileName)
            if (file.exists()) {
                oldestTime = minOf(oldestTime, file.lastModified())
            } else {
                // If any file doesn't exist, return a very old date
                return "2020-01-01T00:00:00Z"
            }
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return if (oldestTime == Long.MAX_VALUE) {
            "2020-01-01T00:00:00Z"
        } else {
            dateFormat.format(Date(oldestTime))
        }
    }

    fun getLastUpdateTimeFormatted(): String {
        val files = listOf(
            "weekdays_east.csv",
            "weekdays_west.csv",
            "saturdays_east.csv",
            "saturdays_west.csv",
            "sundays_east.csv",
            "sundays_west.csv"
        )

        var oldestTime = Long.MAX_VALUE
        files.forEach { fileName ->
            val file = File(getRegularSchedulesDirectory(), fileName)
            if (file.exists()) {
                oldestTime = minOf(oldestTime, file.lastModified())
            } else {
                // If any file doesn't exist, return a message indicating no updates
                return "Never updated"
            }
        }

        return if (oldestTime == Long.MAX_VALUE) {
            "Never updated"
        } else {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            "Last updated: ${dateFormat.format(Date(oldestTime))}"
        }
    }

    fun getSpecialScheduleFile(date: String, fileName: String): File? {
        val file = File(getSpecialSchedulesDirectory(date), fileName)
        val exists = file.exists()
        return if (exists) file else null
    }

    fun cleanupOldSpecialSchedules(currentDate: String) {
        val specialDir = File(getSchedulesDirectory(), "special")
        if (specialDir.exists() && specialDir.isDirectory) {
            specialDir.listFiles()?.forEach { subDir ->
                if (subDir.isDirectory && subDir.name != currentDate) {
                    try {
                        subDir.deleteRecursively()
                    } catch (e: Exception) {
                        Log.e("[cleanupOldSpecialSchedules]", "Failed to delete ${subDir.absolutePath}: ", e)
                    }
                }
            }
        }
    }

}
