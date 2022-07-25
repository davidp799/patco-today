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
        "http://www.ridepatco.org/stations/15th.asp",
        "http://www.ridepatco.org/stations/12th.asp",
        "http://www.ridepatco.org/stations/9th.asp",
        "http://www.ridepatco.org/stations/8th.asp",
        "http://www.ridepatco.org/stations/cityhall.asp",
        "http://www.ridepatco.org/stations/broadway.asp",
        "http://www.ridepatco.org/stations/ferryave.asp",
        "http://www.ridepatco.org/stations/collingswood.asp",
        "http://www.ridepatco.org/stations/westmont.asp",
        "http://www.ridepatco.org/stations/haddonfield.asp",
        "http://www.ridepatco.org/stations/woodcrest.asp",
        "http://www.ridepatco.org/stations/ashland.asp",
        "http://www.ridepatco.org/stations/lindenwold.asp"
    )

    val stationList = _stationList
    val stationLinks = _stationLinks
}