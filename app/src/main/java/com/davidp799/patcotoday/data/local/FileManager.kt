package com.davidp799.patcotoday.data.local

import android.content.Context
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
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val directory = if (isSpecial && date != null) {
                        getSpecialSchedulesDirectory(date)
                    } else {
                        getRegularSchedulesDirectory()
                    }

                    val file = File(directory, fileName)
                    val inputStream = response.body?.byteStream()
                    val outputStream = FileOutputStream(file)

                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun getLastModifiedTime(fileName: String): String {
        val file = File(getRegularSchedulesDirectory(), fileName)
        return if (file.exists()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            dateFormat.format(Date(file.lastModified()))
        } else {
            // Return a very old date if file doesn't exist
            "2020-01-01T00:00:00Z"
        }
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

    fun fileExists(fileName: String): Boolean {
        val file = File(getRegularSchedulesDirectory(), fileName)
        return file.exists()
    }

    fun getSpecialScheduleFile(date: String, fileName: String): File? {
        val file = File(getSpecialSchedulesDirectory(date), fileName)
        return if (file.exists()) file else null
    }
}
