package com.davidp799.patcotoday.data.models

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("special_schedules")
    val specialSchedules: SpecialSchedules?,
    @SerializedName("regular_schedules")
    val regularSchedules: RegularSchedules?,
    val message: String?
)

data class SpecialSchedules(
    @SerializedName("schedule_date")
    val scheduleDate: String,
    @SerializedName("pdf_url")
    val pdfUrl: String,
    @SerializedName("eastbound_url")
    val eastboundUrl: String,
    @SerializedName("westbound_url")
    val westboundUrl: String,
    @SerializedName("expires_in_seconds")
    val expiresInSeconds: Int
)

data class RegularSchedules(
    val updated: Boolean,
    @SerializedName("last_modified")
    val lastModified: String?,
    val urls: ScheduleUrls?,
    @SerializedName("expires_in_seconds")
    val expiresInSeconds: Int?
)

data class ScheduleUrls(
    @SerializedName("weekdays_east_url")
    val weekdaysEastUrl: String,
    @SerializedName("weekdays_west_url")
    val weekdaysWestUrl: String,
    @SerializedName("saturdays_east_url")
    val saturdaysEastUrl: String,
    @SerializedName("saturdays_west_url")
    val saturdaysWestUrl: String,
    @SerializedName("sundays_east_url")
    val sundaysEastUrl: String,
    @SerializedName("sundays_west_url")
    val sundaysWestUrl: String
)
