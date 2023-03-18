package com.davidp799.patcotoday.ui.schedules

import androidx.lifecycle.ViewModel
import com.davidp799.patcotoday.utils.Arrival
import com.davidp799.patcotoday.utils.Schedules
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.collections.ArrayList

class SchedulesViewModel : ViewModel() {
    // Station options
    var isReversed = false
    // Dates
    private var today: LocalDate = LocalDate.now()
    private var dayOfWeek: DayOfWeek = today.dayOfWeek
    var dayOfWeekNumber = dayOfWeek.value
    // Station Data
    var fromIndex = 0
    var fromString = "Lindenwold"
    var toIndex = 12
    var toString = "15-16th & Locust"
    // directory
    val directory: String = "/data/data/com.davidp799.patcotoday/files/data/"
    // Job Management
    var internet = false
    var special = false
    var downloaded = false
    var converted = false
    var schedules = Schedules()

    // Background Lists - arrivals list
    var schedulesArrayList = ArrayList<Arrival>()

    // Background Lists
    var specialEastBound = ArrayList<String>()
    var specialWestBound = ArrayList<String>()
    var specialText = ArrayList<String>()
    var specialURLs = ArrayList<String>()
    var specialTexts = ArrayList<String>()
    var specialFromToTimes = ArrayList<String>()
    var runnableConvertedStrings = ArrayList<String>()
    var parsedArrivals = ArrayList<ArrayList<String>>()
    var specialSchedulesArrayList = ArrayList<Arrival>()

    // List objects
    val stationOptions = listOf(
        "Lindenwold",
        "Ashland",
        "Woodcrest",
        "Haddonfield",
        "Westmont",
        "Collingswood",
        "Ferry Avenue",
        "Broadway",
        "City Hall",
        "8th and Market",
        "9-10th & Locust",
        "12-13th & Locust",
        "15-16th & Locust",
    )
    


}