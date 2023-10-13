package com.davidp799.patcotoday.ui.map

import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    private val _stationList = arrayOf(
        "Lindenwold", "Ashland", "Woodcrest", "Haddonfield",
        "Westmont", "Collingswood", "Ferry Avenue", "Broadway",
        "City Hall", "8th & Market", "9/10th & Locust",
        "12/13th & Locust", "15/16th & Locust"
    )
    private val _stationLinks = arrayOf(
        "https://www.ridepatco.org/stations/lindenwold.asp",
        "https://www.ridepatco.org/stations/ashland.asp",
        "https://www.ridepatco.org/stations/woodcrest.asp",
        "https://www.ridepatco.org/stations/haddonfield.asp",
        "https://www.ridepatco.org/stations/westmont.asp",
        "https://www.ridepatco.org/stations/collingswood.asp",
        "https://www.ridepatco.org/stations/ferryave.asp",
        "https://www.ridepatco.org/stations/broadway.asp",
        "https://www.ridepatco.org/stations/cityhall.asp",
        "https://www.ridepatco.org/stations/8th.asp",
        "https://www.ridepatco.org/stations/9th.asp",
        "https://www.ridepatco.org/stations/12th.asp",
        "https://www.ridepatco.org/stations/15th.asp"
    )

    val stationList = _stationList
    val stationLinks = _stationLinks
}