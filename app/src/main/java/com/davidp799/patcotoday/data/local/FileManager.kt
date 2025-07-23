package com.davidp799.patcotoday.data.local

import android.content.Context
import android.util.Log
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
                Log.d("[ApiDebug]", "Starting file download - URL: $url, File: $fileName, Special: $isSpecial, Date: $date")

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                Log.d("[ApiDebug]", "File download response - Success: ${response.isSuccessful}, Code: ${response.code}, Content Length: ${response.body?.contentLength()}")

                if (response.isSuccessful) {
                    val directory = if (isSpecial && date != null) {
                        getSpecialSchedulesDirectory(date)
                    } else {
                        getRegularSchedulesDirectory()
                    }

                    val file = File(directory, fileName)
                    Log.d("[ApiDebug]", "Saving file to: ${file.absolutePath}")

                    val inputStream = response.body?.byteStream()
                    val outputStream = FileOutputStream(file)

                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    Log.d("[ApiDebug]", "File saved successfully - Size: ${file.length()} bytes, Path: ${file.absolutePath}")
                    true
                } else {
                    Log.e("[ApiDebug]", "File download failed - Code: ${response.code}, URL: $url")
                    false
                }
            } catch (e: Exception) {
                Log.e("[ApiDebug]", "File download exception for $fileName: ${e.message}", e)
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

    fun getSpecialScheduleFile(date: String, fileName: String): File? {
        Log.d("[ApiDebug]", "Looking for special schedule file - Date: $date, File: $fileName")
        val file = File(getSpecialSchedulesDirectory(date), fileName)
        val exists = file.exists()
        Log.d("[ApiDebug]", "Special schedule file exists: $exists, Path: ${file.absolutePath}")
        return if (exists) file else null
    }
}
