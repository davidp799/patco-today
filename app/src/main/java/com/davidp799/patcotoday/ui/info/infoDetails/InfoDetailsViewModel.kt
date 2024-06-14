package com.davidp799.patcotoday.ui.info.infoDetails

import androidx.lifecycle.ViewModel

class InfoDetailsViewModel : ViewModel() {
    private val _infoDetailsList = mapOf(
        "Fares" to mapOf(
            "title" to "Fares",
            "subTitle" to "Effective 7/1/2011 to Present",
            "description" to "The distance you travel determines your fare. One-way and round-trip tickets can be purchased with cash, a credit/debit card or a FREEDOM Card from self-service ticket vending machines (TVMs) located at each station.",
            "childFares" to "Children four (4) and under ride free when accompanied by a fare-paying adult.",
            "reducedFareProgram" to "A Reduced Fare Program (RFP) is available to senior citizens, people with disabilities, and Medicare card holders. The program allows eligible individuals to travel between any two PATCO stations during off-peak hours for \$0.70, regardless of distance traveled. The reduced fare is set as half the lowest full fare.",
            "reducedFareProgramUrl" to "http://www.ridepatco.org/schedules/freedom_reduced_fare.html",
            "farePaymentOptions" to "Fare gates accept FREEDOM Cards, SHARE Cards and paper tickets.",
            "farePaymentOptionsUrl" to "http://www.ridepatco.org/schedules/FREEDOM.html"
        )
    )
    val infoDetailsList = _infoDetailsList
}
