package com.davidp799.patcotoday.ui.map

import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    private val _stationList = arrayOf(
        "Lindenwold", "Ashland", "Woodcrest", "Haddonfield",
        "Westmont", "Collingswood", "Ferry Avenue", "Broadway",
        "City Hall", "Franklin Square", "8th & Market", "9/10th & Locust",
        "12/13th & Locust", "15/16th & Locust"
    )
    val stationList = _stationList
}
