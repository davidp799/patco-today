package com.davidp799.patcotoday.ui.screens.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.davidp799.patcotoday.ui.screens.models.*

class StationMapViewModel : ViewModel() {

    private val _selectedStation = mutableStateOf<Station?>(null)
    val selectedStation: State<Station?> = _selectedStation

    private val _stations = listOf(
        Station(
            id = "lindenwold",
            name = "Lindenwold",
            fullName = "Lindenwold Station",
            address = "901 N. Berlin Road, Lindenwold, NJ 08021",
            amenities = StationAmenities(
                elevator = "Elevator",
                escalator = "Escalator",
                bikeRacks = "Bike Racks",
                taxiService = "Taxi Service",
                parking = "Parking"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            meters = "",
            walkingDistance = "UMDNJ-School of Osteopathic Medicine\nKennedy University Hospital",
            fareZone = FareZone.LINDENWOLD_GROUP,
            fareInfo = FareInfo("$1.60", "$3.20", "$3.00", "$6.00")
        ),
        Station(
            id = "ashland",
            name = "Ashland",
            fullName = "Ashland Station",
            address = "2 Burnt Mill Road, Voorhees, NJ",
            amenities = StationAmenities(
                escalator = "Escalator",
                bikeRacks = "Bike Racks",
                taxiService = "Taxi Service",
                parking = "Parking"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            meters = "25¢ for 2 hours. (Use Quarters Only)",
            walkingDistance = "Voorhees Town Center",
            fareZone = FareZone.LINDENWOLD_GROUP,
            fareInfo = FareInfo("$1.60", "$3.20", "$3.00", "$6.00")
        ),
        Station(
            id = "woodcrest",
            name = "Woodcrest",
            fullName = "Woodcrest Station",
            address = "200 Tindale Avenue, Cherry Hill, NJ 08003",
            amenities = StationAmenities(
                elevator = "Elevator WB",
                escalator = "Escalator WB",
                bikeRacks = "Bike Racks",
                taxiService = "Taxi Service",
                parking = "Parking"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            meters = "25¢ for 2 hours. (Use Quarters Only)",
            walkingDistance = "",
            fareZone = FareZone.LINDENWOLD_GROUP,
            fareInfo = FareInfo("$1.60", "$3.20", "$3.00", "$6.00")
        ),
        Station(
            id = "haddonfield",
            name = "Haddonfield",
            fullName = "Haddonfield Station",
            address = "Kings Highway West and Washington Avenue, Haddonfield, NJ",
            amenities = StationAmenities(
                elevator = "Elevator",
                escalator = "Escalator",
                bikeRacks = "Bike Racks",
                taxiService = "Taxi Service",
                parking = "Parking"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            meters = "25¢ for 2 hours. (Use Quarters Only)",
            walkingDistance = "Shopping & Fine Dining\nHistoric Indian King Tavern\nHadrosaurus Foulkii Dinosaur",
            fareZone = FareZone.HADDONFIELD_GROUP,
            fareInfo = FareInfo("$1.60", "$3.20", "$2.60", "$5.20")
        ),
        Station(
            id = "westmont",
            name = "Westmont",
            fullName = "Westmont Station",
            address = "100 Stoy Avenue, Haddon Township, NJ",
            amenities = StationAmenities(
                escalator = "Up Escalator",
                bikeRacks = "Bike Racks",
                taxiService = "Taxi Service",
                parking = "Parking"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            meters = "25¢ for 2 hours. (Use Quarters Only)",
            walkingDistance = "Shopping & Fine Dining along Haddon Avenue",
            fareZone = FareZone.HADDONFIELD_GROUP,
            fareInfo = FareInfo("$1.60", "$3.20", "$2.60", "$5.20")
        ),
        Station(
            id = "collingswood",
            name = "Collingswood",
            fullName = "Collingswood Station",
            address = "100 Lees Avenue, Collingswood, NJ",
            amenities = StationAmenities(
                escalator = "Up Escalator",
                bikeRacks = "Bike Racks",
                taxiService = "Taxi Service",
                parking = "Parking"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            meters = "",
            walkingDistance = "Shopping\nDining\nFarmer's Market (seasonal - Saturday a.m.)\n2nd Saturdays (art, music, etc.)",
            fareZone = FareZone.HADDONFIELD_GROUP,
            fareInfo = FareInfo("$1.60", "$3.20", "$2.60", "$5.20")
        ),
        Station(
            id = "ferry_avenue",
            name = "Ferry Avenue",
            fullName = "Ferry Avenue Station",
            address = "410 Copewood St, Camden, NJ 08104",
            amenities = StationAmenities(
                elevator = "Elevator",
                escalator = "Escalator",
                bikeRacks = "Bike Racks",
                taxiService = "Taxi Service",
                parking = "Parking"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            meters = "",
            walkingDistance = "Lady of Lourdes Hospital\nCamden Historical Society\nHistoric Harleigh Cemetery",
            fareZone = FareZone.FERRY_AVENUE,
            fareInfo = FareInfo("$1.60", "$3.20", "$2.25", "$4.50")
        ),
        Station(
            id = "broadway",
            name = "Broadway",
            fullName = "Broadway Station\n(Walter Rand Transportation Center)",
            address = "100 South Broadway, Camden, NJ 08103",
            amenities = StationAmenities(
                elevator = "Elevator",
                escalator = "Up Escalator",
                bikeRacks = "Bike Racks at W. Headhouse",
                taxiService = "Taxi Service"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "",
            meters = "",
            walkingDistance = "Cooper Hospital\nWalt Whitman House\nFreedom Mortgage Pavilion\nRiverLINE to Trenton",
            fareZone = FareZone.BROADWAY_GROUP,
            fareInfo = FareInfo("$1.60", "$3.20", "$1.40", "$2.80")
        ),
        Station(
            id = "city_hall",
            name = "City Hall",
            fullName = "City Hall Station",
            address = "Market Street and North 5th Street, Camden NJ",
            amenities = StationAmenities(
                elevator = "Elevator",
                bikeRacks = "Bike Racks"
            ),
            hours = "Station is open daily from 5 a.m. to midnight.",
            gatedParking = "",
            meters = "",
            walkingDistance = "Rutgers-Camden, Camden County College\nRowan University\nCampbell's Field (RiverSharks baseball)\nCamden Waterfront\nHall of Justice (for jury duty)",
            fareZone = FareZone.BROADWAY_GROUP,
            fareInfo = FareInfo("$1.60", "$3.20", "$1.40", "$2.80")
        ),
        Station(
            id = "franklin_square",
            name = "Franklin Square",
            fullName = "Franklin Square Station",
            address = "7th & Race Streets, Philadelphia, PA",
            amenities = StationAmenities(
                elevator = "Elevator",
                escalator = "Escalator",
                bikeRacks = "Bike Racks"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "",
            meters = "",
            walkingDistance = "African American Museum\nChinatown Business District\nFranklin Music Hall\nFranklin Square Park\nIndependence Visitor Center\nNational Constitution Center\nPhiladelphia's Old City",
            fareZone = FareZone.PHILADELPHIA,
            fareInfo = FareInfo("$3.00", "$6.00", "$1.40", "$2.80")
        ),
        Station(
            id = "8th_market",
            name = "8th & Market",
            fullName = "8th & Market Street Station",
            address = "8th & Market Street, Philadelphia, PA",
            amenities = StationAmenities(
                elevator = "Elevator",
                escalator = "Escalator",
                bikeRacks = "Bike Racks"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "",
            meters = "",
            walkingDistance = "Historic District\nGallery Shopping\nPA Convention Center\nSEPTA's Market East Station\nSEPTA's Market Frankford Line to University City",
            fareZone = FareZone.PHILADELPHIA,
            fareInfo = FareInfo("$3.00", "$6.00", "$1.40", "$2.80")
        ),
        Station(
            id = "9_10_locust",
            name = "9/10th & Locust",
            fullName = "9/10th & Locust Street Station",
            address = "9th & Locust Street, Philadelphia, PA",
            amenities = StationAmenities(
                elevator = "Elevator",
                escalator = "Escalator",
                bikeRacks = "Bike Racks"
            ),
            hours = "Station is open daily from 4:15 a.m. to 12:07 a.m.",
            gatedParking = "",
            meters = "",
            walkingDistance = "Jefferson Hospital\nPennsylvania Hospital\nWills Eye Hospitals\nWalnut Street Theater",
            fareZone = FareZone.PHILADELPHIA,
            fareInfo = FareInfo("$3.00", "$6.00", "$1.40", "$2.80")
        ),
        Station(
            id = "12_13_locust",
            name = "12/13th & Locust",
            fullName = "12/13th & Locust Street Station",
            address = "12th & Locust Street, Philadelphia, PA",
            amenities = StationAmenities(
                escalator = "Up Escalator",
                bikeRacks = "Bike Racks"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "",
            meters = "",
            walkingDistance = "Forrest Theater\nSEPTA's Broad Street Line (to travel to stadiums)",
            fareZone = FareZone.PHILADELPHIA,
            fareInfo = FareInfo("$3.00", "$6.00", "$1.40", "$2.80")
        ),
        Station(
            id = "15_16_locust",
            name = "15/16th & Locust",
            fullName = "15/16th & Locust Street Station",
            address = "15th & Locust Street, Philadelphia, PA",
            amenities = StationAmenities(
                elevator = "Elevator",
                escalator = "Up Escalator",
                bikeRacks = "Bike Racks"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "",
            meters = "",
            walkingDistance = "Avenue of the Arts\nKimmel Center\nAcademy of Music\nMerriam Theater\nWilma Theaters\nUniversity of the Arts\nRittenhouse Square",
            fareZone = FareZone.PHILADELPHIA,
            fareInfo = FareInfo("$3.00", "$6.00", "$1.40", "$2.80")
        )
    )

    val stations: List<Station> = _stations

    fun selectStation(station: Station?) {
        _selectedStation.value = station
    }

    fun clearSelection() {
        _selectedStation.value = null
    }
}
