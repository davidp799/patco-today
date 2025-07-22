package com.davidp799.patcotoday.data.repository

import android.content.Context
import android.util.Log
import com.davidp799.patcotoday.data.api.ScheduleApiService
import com.davidp799.patcotoday.data.local.FileManager
import com.davidp799.patcotoday.data.local.CsvScheduleParser
import com.davidp799.patcotoday.data.models.ApiResponse
import com.davidp799.patcotoday.utils.Arrival
import com.davidp799.patcotoday.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class ScheduleRepository(context: Context) {

    private val fileManager = FileManager(context)
    private val csvParser = CsvScheduleParser(context)
    private val apiService: ScheduleApiService

    // API key now read from BuildConfig, which gets it from local.properties
    private val apiKey = BuildConfig.API_KEY
    private val baseUrl = "https://pyy0z7hm81.execute-api.us-east-1.amazonaws.com/"

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ScheduleApiService::class.java)
    }

    suspend fun fetchAndUpdateSchedules(): Result<ApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val today = getCurrentDate()
                val lastUpdated = fileManager.getOldestLastModified()

                Log.d("[ApiDebug]", "Starting API call to fetch schedules - Date: $today, Last Updated: $lastUpdated")

                val response = apiService.getSchedules(
                    scheduleDate = today,
                    lastUpdated = lastUpdated,
                    apiKey = apiKey
                )

                Log.d("[ApiDebug]", "API response received - Success: ${response.isSuccessful}, Code: ${response.code()}")

                if (response.isSuccessful) {
                    val apiResponse = response.body()!!
                    Log.d("[ApiDebug]", "API response parsed - Special schedules: ${apiResponse.specialSchedules != null}, Regular schedules updated: ${apiResponse.regularSchedules?.updated}")

                    // Download special schedules if available
                    apiResponse.specialSchedules?.let { special ->
                        Log.d("[ApiDebug]", "Special schedules found for date: ${special.scheduleDate}")
                        downloadSpecialSchedules(special.scheduleDate, special.eastboundUrl, special.westboundUrl)
                    }

                    // Download regular schedules if updated
                    apiResponse.regularSchedules?.let { regular ->
                        if (regular.updated && regular.urls != null) {
                            Log.d("[ApiDebug]", "Regular schedules need updating - downloading new files")
                            downloadRegularSchedules(regular.urls)
                        } else {
                            Log.d("[ApiDebug]", "Regular schedules are up to date - no download needed")
                        }
                    }

                    Result.success(apiResponse)
                } else {
                    Log.e("[ApiDebug]", "API call failed with code: ${response.code()}")
                    Result.failure(Exception("API call failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e("[ApiDebug]", "API call exception: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getScheduleForRoute(fromStation: String, toStation: String): List<Arrival> {
        Log.d("[ApiDebug]", "Getting arrival times for route: $fromStation -> $toStation")
        val arrivals = csvParser.parseScheduleForRoute(fromStation, toStation)
        Log.d("[ApiDebug]", "Found ${arrivals.size} arrival times for route: $fromStation -> $toStation")
        return arrivals
    }

    private suspend fun downloadSpecialSchedules(date: String, eastboundUrl: String, westboundUrl: String) {
        Log.d("[ApiDebug]", "Downloading special schedule - Eastbound from: $eastboundUrl")
        fileManager.downloadAndSaveFile(
            url = eastboundUrl,
            fileName = "special_schedule_eastbound.csv",
            isSpecial = true,
            date = date
        )

        Log.d("[ApiDebug]", "Downloading special schedule - Westbound from: $westboundUrl")
        fileManager.downloadAndSaveFile(
            url = westboundUrl,
            fileName = "special_schedule_westbound.csv",
            isSpecial = true,
            date = date
        )
    }

    private suspend fun downloadRegularSchedules(urls: com.davidp799.patcotoday.data.models.ScheduleUrls) {
        val downloads = listOf(
            urls.weekdaysEastUrl to "weekdays_east.csv",
            urls.weekdaysWestUrl to "weekdays_west.csv",
            urls.saturdaysEastUrl to "saturdays_east.csv",
            urls.saturdaysWestUrl to "saturdays_west.csv",
            urls.sundaysEastUrl to "sundays_east.csv",
            urls.sundaysWestUrl to "sundays_west.csv"
        )

        Log.d("[ApiDebug]", "Starting download of ${downloads.size} regular schedule files")
        downloads.forEach { (url, fileName) ->
            Log.d("[ApiDebug]", "Downloading regular schedule file: $fileName from $url")
            fileManager.downloadAndSaveFile(url, fileName)
        }
        Log.d("[ApiDebug]", "Completed download of all regular schedule files")
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
