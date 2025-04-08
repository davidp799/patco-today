package com.davidp799.patcotoday.ui.map.mapItemDetails

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.davidp799.patcotoday.R
import com.davidp799.patcotoday.databinding.FragmentMapDetailsBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.transition.MaterialContainerTransform

class MapItemDetailsFragment : Fragment() {
    private var _binding: FragmentMapDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = 400
            scrimColor = Color.TRANSPARENT
        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            duration = 400
            scrimColor = Color.TRANSPARENT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val stationName = arguments?.getString("stationName")
        val stationDetailsViewModel =
            ViewModelProvider(this)[MapItemDetailsViewModel::class.java]
        _binding = FragmentMapDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setLayout(root, stationDetailsViewModel.stationDetailsList[stationName])
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val transitionName = "station_${arguments?.getString("stationName")}"
        val detailsContainer = view.findViewById<MaterialCardView>(R.id.details_container)
        ViewCompat.setTransitionName(detailsContainer, transitionName)
    }

    private fun setLayout(view: View, stationDetails: Map<String, Any>?) {
        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        titleTextView.text = stationDetails?.get("title").toString()

        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
        descriptionTextView.text = stationDetails?.get("description").toString()

        descriptionTextView.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(descriptionTextView.text.toString())}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            val packageManager = requireContext().packageManager
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                val clip = ClipData.newPlainText("station address", descriptionTextView.text.toString())
                getSystemService(requireContext(), ClipboardManager::class.java)?.setPrimaryClip(
                    clip
                )
                Toast.makeText(
                    requireContext(),
                    "Address copied to clipboard",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val elevatorContainer = view.findViewById<RelativeLayout>(R.id.elevatorContainer)
        val escalatorContainer = view.findViewById<RelativeLayout>(R.id.escalatorContainer)
        val bikeRacksContainer = view.findViewById<RelativeLayout>(R.id.bikeRacksContainer)
        val taxiContainer = view.findViewById<RelativeLayout>(R.id.taxiContainer)
        val parkingContainer = view.findViewById<RelativeLayout>(R.id.parkingContainer)

        val amenities = stationDetails?.get("amenities") as Map<*, *>
        if (amenities.contains("elevator")) {
            elevatorContainer.visibility = View.VISIBLE
            val elevatorTextView = elevatorContainer.findViewById<TextView>(R.id.elevatorTextView)
            elevatorTextView.text = amenities["elevator"].toString()
        } else {
            elevatorContainer.visibility = View.GONE
        }

        if (amenities.contains("escalator")) {
            escalatorContainer.visibility = View.VISIBLE
            val escalatorTextView = escalatorContainer.findViewById<TextView>(R.id.escalatorTextView)
            escalatorTextView.text = amenities["escalator"].toString()
        } else {
            escalatorContainer.visibility = View.GONE
        }

        if (amenities.contains("bikeRacks")) {
            bikeRacksContainer.visibility = View.VISIBLE
            val bikeRacksTextView = bikeRacksContainer.findViewById<TextView>(R.id.bikeRacksTextView)
            bikeRacksTextView.text = amenities["bikeRacks"].toString()
        } else {
            bikeRacksContainer.visibility = View.GONE
        }

        if (amenities.contains("taxiService")) {
            taxiContainer.visibility = View.VISIBLE
            val taxiTextView = taxiContainer.findViewById<TextView>(R.id.taxiTextView)
            taxiTextView.text = amenities["taxiService"].toString()
        } else {
            taxiContainer.visibility = View.GONE
        }

        if (amenities.contains("parking")) {
            parkingContainer.visibility = View.VISIBLE
            val parkingTextView = parkingContainer.findViewById<TextView>(R.id.parkingTextView)
            parkingTextView.text = amenities["parking"].toString()
        } else {
            parkingContainer.visibility = View.GONE
        }

        val stationHoursLinearLayout = view.findViewById<LinearLayout>(R.id.stationHoursLinearLayout)
        val stationHoursTextViewBody = view.findViewById<TextView>(R.id.stationHoursTextViewBody)
        var stationHours = stationDetails["hours"] as String
        if (stationHours.isNotEmpty()) {
            stationHoursLinearLayout.visibility = View.VISIBLE
            stationHoursTextViewBody.text = stationHours
        } else {
            stationHours = "Station is open 24/7."
            stationHoursTextViewBody.text = stationHours
        }

//        Fares section
        var newJerseyOneWayFare: String
        var newJerseyRoundTripFare: String
        var philadelphiaOneWayFare: String
        var philadelphiaRoundTripFare: String
        val stationTitle = stationDetails["title"].toString()
        if (stationTitle.contains("Broadway Station")) {
            val broadwayToCityHallRow = view.findViewById<TableRow>(R.id.broadwayToCityHallRow)
            broadwayToCityHallRow.visibility = View.VISIBLE
        } else {
            val broadwayToCityHallRow = view.findViewById<TableRow>(R.id.broadwayToCityHallRow)
            broadwayToCityHallRow.visibility = View.GONE
        }

        when (stationTitle) {
            in arrayOf("Lindenwold Station", "Ashland Station", "Woodcrest Station") -> {
                newJerseyOneWayFare = "$1.60"
                newJerseyRoundTripFare = "$3.20"
                philadelphiaOneWayFare = "$3.00"
                philadelphiaRoundTripFare = "$6.00"
            }
            in arrayOf("Haddonfield Station", "Westmont Station", "Collingswood Station") -> {
                newJerseyOneWayFare = "$1.60"
                newJerseyRoundTripFare = "$3.20"
                philadelphiaOneWayFare = "$2.60"
                philadelphiaRoundTripFare = "$5.20"
            }
            "Ferry Avenue Station" -> {
                newJerseyOneWayFare = "$1.60"
                newJerseyRoundTripFare = "$3.20"
                philadelphiaOneWayFare = "$2.25"
                philadelphiaRoundTripFare = "$4.50"
            }
            "Broadway Station (Walter Rand Transportation Center)", "City Hall Station" -> {
                newJerseyOneWayFare = "$1.60"
                newJerseyRoundTripFare = "$3.20"
                philadelphiaOneWayFare = "$1.40"
                philadelphiaRoundTripFare = "$2.80"
            }
            else -> { // philadelphia to any
                newJerseyOneWayFare = "$3.00"
                newJerseyRoundTripFare = "$6.00"
                philadelphiaOneWayFare = "$1.40"
                philadelphiaRoundTripFare = "$2.80"
            }
        }

        val thisStationToPhiladelphiaTextView = view.findViewById<TextView>(R.id.thisStationToPhiladelphia)
        thisStationToPhiladelphiaTextView.text = stationTitle
        val thisStationToPhiladelphiaOneWay = view.findViewById<TextView>(R.id.thisStationToPhiladelphiaOneWay)
        thisStationToPhiladelphiaOneWay.text = philadelphiaOneWayFare
        val thisStationToPhiladelphiaRoundTrip = view.findViewById<TextView>(R.id.thisStationToPhiladelphiaRoundTrip)
        thisStationToPhiladelphiaRoundTrip.text = philadelphiaRoundTripFare

        val thisStationToNewJerseyTextView = view.findViewById<TextView>(R.id.thisStationToNewJersey)
        thisStationToNewJerseyTextView.text = stationTitle
        val thisStationToNewJerseyOneWay = view.findViewById<TextView>(R.id.thisStationToNewJerseyOneWay)
        thisStationToNewJerseyOneWay.text = newJerseyOneWayFare
        val thisStationToNewJerseyRoundTrip = view.findViewById<TextView>(R.id.thisStationToNewJerseyRoundTrip)
        thisStationToNewJerseyRoundTrip.text = newJerseyRoundTripFare

        val gatedParkingLinearLayout = view.findViewById<LinearLayout>(R.id.gatedParkingLinearLayout)
        val gatedParkingTextViewBody = view.findViewById<TextView>(R.id.gatedParkingTextViewBody)
        val gatedParking = stationDetails["gatedParking"] as String
        if (gatedParking.isNotEmpty()) {
            gatedParkingLinearLayout.visibility = View.VISIBLE
            gatedParkingTextViewBody.text = gatedParking
        } else {
            gatedParkingLinearLayout.visibility = View.GONE
        }

        val metersLinearLayout = view.findViewById<LinearLayout>(R.id.metersLinearLayout)
        val metersTextViewBody = view.findViewById<TextView>(R.id.metersTextViewBody)
        val meters = stationDetails["meters"] as String
        if (meters.isNotEmpty()) {
            metersLinearLayout.visibility = View.VISIBLE
            metersTextViewBody.text = meters
        } else {
            metersLinearLayout.visibility = View.GONE
        }

        val walkingDistanceLinearLayout = view.findViewById<LinearLayout>(R.id.walkingDistanceLinearLayout)
        val walkingDistanceTextViewBody = view.findViewById<TextView>(R.id.walkingDistanceTextViewBody)
        val walkingDistance = stationDetails["walkingDistance"] as String
        if (walkingDistance.isNotEmpty()) {
            walkingDistanceLinearLayout.visibility = View.VISIBLE
            walkingDistanceTextViewBody.text = walkingDistance
        } else {
            walkingDistanceLinearLayout.visibility = View.GONE
        }
    }
}