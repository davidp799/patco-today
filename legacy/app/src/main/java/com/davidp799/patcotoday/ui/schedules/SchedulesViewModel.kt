package com.davidp799.patcotoday.ui.schedules

import androidx.lifecycle.ViewModel
import com.davidp799.patcotoday.utils.Arrival
import com.davidp799.patcotoday.utils.Schedules
import com.davidp799.patcotoday.utils.Trip
import kotlin.collections.ArrayList

class SchedulesViewModel : ViewModel() {
    // Station options
    var isReversed = false
    // Station Data
    var fromIndex = 0
    var fromString = "Lindenwold"
    var toIndex = 13
    var toString = "15-16th & Locust"
    // directory
    val directory: String = "/data/data/com.davidp799.patcotoday/files/data/"
    // Job Management
    var internet = false
    var special = false
    var downloaded = false
    var converted = false
    var schedules = Schedules()
    var automaticDownloads = true

    // Background Lists - arrivals list
    var schedulesArrayList = ArrayList<Arrival>()

    // Background Lists
    var specialEastBound = ArrayList<Trip>()
    var specialWestBound = ArrayList<Trip>()
    var specialText = ArrayList<String>()
    var specialURLs = ArrayList<String>()
    var specialTexts = ArrayList<String>()
    var specialFromToTimes = ArrayList<String>()
    var runnableConvertedStrings = ArrayList<String>()
    var parsedArrivals = ArrayList<ArrayList<Trip>>()
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
        "Franklin Square",
        "8th and Market",
        "9-10th & Locust",
        "12-13th & Locust",
        "15-16th & Locust",
    )
}