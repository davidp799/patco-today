package com.davidp799.patcotoday.data.repository

import android.content.Context
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

                val response = apiService.getSchedules(
                    scheduleDate = today,
                    lastUpdated = lastUpdated,
                    apiKey = apiKey
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()!!

                    // Download special schedules if available
                    apiResponse.specialSchedules?.let { special ->
                        downloadSpecialSchedules(special.scheduleDate, special.eastboundUrl, special.westboundUrl)
                    }

                    // Download regular schedules if updated
                    apiResponse.regularSchedules?.let { regular ->
                        if (regular.updated && regular.urls != null) {
                            downloadRegularSchedules(regular.urls)
                        }
                    }

                    Result.success(apiResponse)
                } else {
                    Result.failure(Exception("API call failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getScheduleForRoute(fromStation: String, toStation: String): List<Arrival> {
        return csvParser.parseScheduleForRoute(fromStation, toStation)
    }

    private suspend fun downloadSpecialSchedules(date: String, eastboundUrl: String, westboundUrl: String) {
        fileManager.downloadAndSaveFile(
            url = eastboundUrl,
            fileName = "special_schedule_eastbound.csv",
            isSpecial = true,
            date = date
        )

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

        downloads.forEach { (url, fileName) ->
            fileManager.downloadAndSaveFile(url, fileName)
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
