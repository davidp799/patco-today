package com.davidp799.patcotoday.ui.info

import androidx.lifecycle.ViewModel

class InfoViewModel : ViewModel() {
    private val _infoLinks = arrayOf(
        "http://www.ridepatco.org/schedules/fares.html",
        "https://www.patcofreedomcard.org/front/account/login.jsp",
        "https://twitter.com/RidePATCO", "tel:+1-856-772-6900",
        "patco@ridepatco.org", "http://www.ridepatco.org/index.asp",
        "http://www.ridepatco.org/schedules/schedules.asp",
        "http://www.ridepatco.org/schedules/alerts_more.asp?page=25",
        "http://www.ridepatco.org/travel/faqs.html", "http://www.ridepatco.org/safety/how_do_i.html"
    )

    private val _infoItems = arrayOf(
        "Fares", "Reload Freedom Card", "Twitter", "Call", "Email", "Website", "Special Schedules",
        "Elevator & Escalator Availability", "FAQ's", "Safety & Security"
    )

    val infoLinks = _infoLinks
    val infoItems = _infoItems
}