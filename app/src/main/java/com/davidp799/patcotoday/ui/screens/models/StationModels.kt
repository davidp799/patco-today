package com.davidp799.patcotoday.ui.screens.models

data class Station(
    val id: String,
    val name: String,
    val fullName: String,
    val address: String,
    val amenities: StationAmenities,
    val hours: String,
    val gatedParking: String,
    val meters: String,
    val walkingDistance: String,
    val fareZone: FareZone,
    val fareInfo: FareInfo
)

data class StationAmenities(
    val elevator: String? = null,
    val escalator: String? = null,
    val bikeRacks: String? = null,
    val taxiService: String? = null,
    val parking: String? = null
) {
    fun hasElevator() = !elevator.isNullOrEmpty()
    fun hasEscalator() = !escalator.isNullOrEmpty()
    fun hasBikeRacks() = !bikeRacks.isNullOrEmpty()
    fun hasTaxiService() = !taxiService.isNullOrEmpty()
    fun hasParking() = !parking.isNullOrEmpty()
}

enum class FareZone {
    LINDENWOLD_GROUP,
    HADDONFIELD_GROUP,
    FERRY_AVENUE,
    BROADWAY_GROUP,
    PHILADELPHIA
}

data class FareInfo(
    val newJerseyOneWay: String,
    val newJerseyRoundTrip: String,
    val philadelphiaOneWay: String,
    val philadelphiaRoundTrip: String
)
