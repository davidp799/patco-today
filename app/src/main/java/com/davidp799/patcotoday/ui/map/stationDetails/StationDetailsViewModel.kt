package com.davidp799.patcotoday.ui.map.stationDetails

import androidx.lifecycle.ViewModel

class StationDetailsViewModel : ViewModel() {
    private val _stationDetailsList = mapOf(
        "Lindenwold" to mapOf(
            "title" to "Lindenwold Station",
            "description" to "901 N. Berlin Road, Lindenwold, NJ 08021",
            "amenities" to mapOf(
                "elevator" to "Elevator",
                "escalator" to "Escalator",
                "bikeRacks" to "Bike Racks",
                "taxiService" to "Taxi Service",
                "parking" to "Parking"
            ),
            "hours" to "",
            "gatedParking" to "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            "walkingDistance" to "UMDNJ-School of Osteopathic Medicine\nKennedy University Hospital"
        ),
        "Ashland" to mapOf(
            "title" to "Ashland Station",
            "description" to "2 Burnt Mill Road, Voorhees, NJ",
            "amenities" to mapOf(
                "escalator" to "Escalator",
                "bikeRacks" to "Bike Racks",
                "taxiService" to "Taxi Service",
                "parking" to "Parking"
            ),
            "hours" to "",
            "gatedParking" to "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            "walkingDistance" to "Voorhees Town Center"
        ),
        "Woodcrest" to mapOf(
            "title" to "Woodcrest Station",
            "description" to "200 Tindale Avenue, Cherry Hill, NJ 08003",
            "amenities" to mapOf(
                "elevator" to "Elevator WB",
                "escalator" to "Escalator WB",
                "bikeRacks" to "Bike Racks",
                "taxiService" to "Taxi Service",
                "parking" to "Parking"
            ),
            "hours" to "",
            "gatedParking" to "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            "walkingDistance" to ""
        ),
        "Haddonfield" to mapOf(
            "title" to "Haddonfield Station",
            "description" to "Kings Highway West and Washington Avenue, Haddonfield, NJ",
            "amenities" to mapOf(
                "elevator" to "Elevator",
                "escalator" to "Escalator",
                "bikeRacks" to "Bike Racks",
                "taxiService" to "Taxi Service",
                "parking" to "Parking"
            ),
            "hours" to "",
            "gatedParking" to "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            "walkingDistance" to "Shopping & Fine Dining\nHistoric Indian King Tavern\nHadrosaurus Foulkii Dinosaur"
        ),
        "Westmont" to mapOf(
            "title" to "Westmont Station",
            "description" to "100 Stoy Avenue, Haddon Township, NJ",
            "amenities" to mapOf(
                "escalator" to "Up Escalator",
                "bikeRacks" to "Bike Racks",
                "taxiService" to "Taxi Service",
                "parking" to "Parking"
            ),
            "hours" to "",
            "gatedParking" to "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            "walkingDistance" to "Shopping & Fine Dining along Haddon Avenue"
        ),
        "Collingswood" to mapOf(
            "title" to "Collingswood Station",
            "description" to "100 Lees Avenue, Collingswood, NJ",
            "amenities" to mapOf(
                "escalator" to "Up Escalator",
                "bikeRacks" to "Bike Racks",
                "taxiService" to "Taxi Service",
                "parking" to "Parking"
            ),
            "hours" to "",
            "gatedParking" to "",
            "walkingDistance" to "Shopping\n" +
                    "Dining\n" +
                    "Farmer's Market (seasonal - Saturday a.m.)\n" +
                    "2nd Saturdays (art, music, etc.)"
        ),
        "Ferry Avenue" to mapOf(
            "title" to "Ferry Avenue Station",
            "description" to "410 Copewood St, Camden, NJ 08104",
            "amenities" to mapOf(
                "elevator" to "Elevator",
                "escalator" to "Escalator",
                "bikeRacks" to "Bike Racks",
                "taxiService" to "Taxi Service",
                "parking" to "Parking"
            ),
            "hours" to "",
            "gatedParking" to "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            "walkingDistance" to "Lady of Lourdes Hospital\n" +
                    "Camden Historical Society\n" +
                    "Historic Harleigh Cemetery"
        ),
        "Broadway" to mapOf(
            "title" to "Broadway Station (Walter Rand Transportation Center)",
            "description" to "100 South Broadway, Camden, NJ 08103",
            "amenities" to mapOf(
                "elevator" to "Elevator",
                "escalator" to "Up Escalator",
                "bikeRacks" to "Bike Racks at W. Headhouse",
                "taxiService" to "Taxi Service"
            ),
            "hours" to "",
            "gatedParking" to "",
            "walkingDistance" to "Cooper Hospital\n" +
                    "Walt Whitman House\n" +
                    "Freedom Mortgage Pavilion\n" +
                    "RiverLINE to Trenton"
        ),
        "City Hall" to mapOf(
            "title" to "City Hall",
            "description" to "Market Street and North 5th Street, Camden NJ",
            "amenities" to mapOf(
                "elevator" to "Elevator",
                "bikeRacks" to "Bike Racks"
            ),
            "hours" to "Station is open daily from 5 a.m. to midnight.",
            "gatedParking" to "",
            "walkingDistance" to "Rutgers-Camden, Camden County College\n" +
                    "Rowan University\n" +
                    "Campbell's Field (RiverSharks baseball)\n" +
                    "Camden Waterfront\n" +
                    "Hall of Justice (for jury duty)"
        ),
        "8th & Market" to mapOf(
            "title" to "8th & Market Street Station",
            "description" to "Market Street and North 5th Street, Camden NJ",
            "amenities" to mapOf(
                "elevator" to "Elevator",
                "escalator" to "Escalator",
                "bikeRacks" to "Bike Racks"
            ),
            "hours" to "",
            "gatedParking" to "",
            "walkingDistance" to "Historic District\n" +
                    "Gallery Shopping\n" +
                    "PA Convention Center\n" +
                    "SEPTA's Market East Station\n" +
                    "SEPTA's Market Frankford Line to University City\n" +
                    "[MORE]"
        ),
        "9/10th & Locust" to mapOf(
            "title" to "9/10th & Locust Street Station",
            "description" to "9th & Locust Street, Philadelphia, PA",
            "amenities" to mapOf(
                "elevator" to "Elevator",
                "escalator" to "Escalator",
                "bikeRacks" to "Bike Racks"
            ),
            "hours" to "Station is open daily from 4:15 a.m. to 12:07 a.m.",
            "gatedParking" to "",
            "walkingDistance" to "Jefferson Hospital\n" +
                    "Pennsylvania Hospital\n" +
                    "Wills Eye Hospitals\n" +
                    "Walnut Street Theater\n" +
                    "[MORE]"
        ),
        "12/13th & Locust" to mapOf(
            "title" to "12/13th & Locust Street Station",
            "description" to "12th & Locust Street, Philadelphia, PA",
            "amenities" to mapOf(
                "escalator" to "Up Escalator",
                "bikeRacks" to "Bike Racks"
            ),
            "hours" to "",
            "gatedParking" to "",
            "walkingDistance" to "Forrest Theater\n" +
                    "SEPTA's Broad Street Line (to travel to stadiums)\n" +
                    "[MORE]"
        ),
        "15/16th & Locust" to mapOf(
            "title" to "15/16th & Locust Street Station",
            "description" to "15th & Locust Street, Philadelphia, PA",
            "amenities" to mapOf(
                "elevator" to "Elevator",
                "escalator" to "Up Escalator",
                "bikeRacks" to "Bike Racks"
            ),
            "hours" to "",
            "gatedParking" to "",
            "walkingDistance" to "Avenue of the Arts\n" +
                    "Kimmel Center\n" +
                    "Academy of Music\n" +
                    "Merriam Theater\n" +
                    "Wilma Theaters\n" +
                    "University of the Arts\n" +
                    "Rittenhouse Square\n" +
                    "[MORE]"
        )
    )
    val stationDetailsList = _stationDetailsList
}