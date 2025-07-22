package com.davidp799.patcotoday.data.api

import com.davidp799.patcotoday.data.models.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ScheduleApiService {
    @GET("prod")
    suspend fun getSchedules(
        @Query("schedule_date") scheduleDate: String,
        @Query("last_updated") lastUpdated: String,
        @Header("x-api-key") apiKey: String
    ): Response<ApiResponse>
}
