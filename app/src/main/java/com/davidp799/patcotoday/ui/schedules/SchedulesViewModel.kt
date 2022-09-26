package com.davidp799.patcotoday.ui.schedules

import androidx.lifecycle.ViewModel
import com.davidp799.patcotoday.utils.Arrival
import com.davidp799.patcotoday.utils.Schedules
import java.util.*
import kotlin.collections.ArrayList

class SchedulesViewModel : ViewModel() {
    // Station options
    var isReversed = false;
    // Station Data
    var weekday =
        Calendar.DAY_OF_WEEK // weekday in java starts on monday
    var fromSelection = 0
    var toSelection = 0
    // directory
    val directory: String = "/data/data/com.davidp799.patcotoday/files/data/"
    // Job Management
    var internet = false
    var special = false
    var downloaded = false
    var converted = false
    var parsed = false
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