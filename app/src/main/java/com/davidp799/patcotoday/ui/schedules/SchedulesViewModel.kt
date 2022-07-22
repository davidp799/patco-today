package com.davidp799.patcotoday.ui.schedules

import androidx.lifecycle.ViewModel
import com.davidp799.patcotoday.utils.Arrival
import com.davidp799.patcotoday.utils.Schedules

class SchedulesViewModel : ViewModel() {
    // Station options
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
    // Background Lists
    var specialEastBound = ArrayList<String>()
    var specialWestBound = ArrayList<String>()
    var specialText = ArrayList<String>()
    var specialURLs = ArrayList<String>()
    var specialTexts = ArrayList<String>()
    var runnableConvertedStrings = ArrayList<String>()
    var parsedArrivals = ArrayList<ArrayList<String>>()
    var schedulesArrayList = ArrayList<Arrival>()
    var specialSchedulesArrayList = ArrayList<Arrival>()
    
    
    
    var stationOptions = listOf(
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