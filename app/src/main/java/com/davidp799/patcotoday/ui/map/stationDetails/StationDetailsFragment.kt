package com.davidp799.patcotoday.ui.map.stationDetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.davidp799.patcotoday.R
import com.davidp799.patcotoday.databinding.FragmentStationDetailsBinding
import com.google.android.material.transition.MaterialSharedAxis

class StationDetailsFragment : Fragment() {
    private var _binding: FragmentStationDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val stationName = arguments?.getString("stationName")
        println("stationId: $stationName")

        val stationDetailsViewModel =
            ViewModelProvider(this)[StationDetailsViewModel::class.java]
        _binding = FragmentStationDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)

        setLayout(root, stationDetailsViewModel.stationDetailsList[stationName])

        return root

    }

    @SuppressLint("SetTextI18n")
    private fun setLayout(view: View, stationDetails: Map<String, Any>?) {
        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        titleTextView.text = stationDetails?.get("title").toString()

        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionTextView)
        descriptionTextView.text = stationDetails?.get("description").toString()

        val elevatorLinearLayout = view.findViewById<LinearLayout>(R.id.elevatorLinearLayout)
        val escalatorLinearLayout = view.findViewById<LinearLayout>(R.id.escalatorLinearLayout)
        val bikeRacksLinearLayout = view.findViewById<LinearLayout>(R.id.bikeRacksLinearLayout)
        val taxiLinearLayout = view.findViewById<LinearLayout>(R.id.taxiLinearLayout)
        val parkingLinearLayout = view.findViewById<LinearLayout>(R.id.parkingLinearLayout)

        val amenities = stationDetails?.get("amenities") as Map<*, *>
        if (amenities.contains("elevator")) {
            elevatorLinearLayout.visibility = View.VISIBLE
            val elevatorTextView = elevatorLinearLayout.findViewById<TextView>(R.id.elevatorTextView)
            elevatorTextView.text = amenities["elevator"].toString()
        } else {
            elevatorLinearLayout.visibility = View.GONE
        }

        if (amenities.contains("escalator")) {
            escalatorLinearLayout.visibility = View.VISIBLE
            val escalatorTextView = escalatorLinearLayout.findViewById<TextView>(R.id.escalatorTextView)
            escalatorTextView.text = amenities["escalator"].toString()
        } else {
            escalatorLinearLayout.visibility = View.GONE
        }

        if (amenities.contains("bikeRacks")) {
            bikeRacksLinearLayout.visibility = View.VISIBLE
            val bikeRacksTextView = bikeRacksLinearLayout.findViewById<TextView>(R.id.bikeRacksTextView)
            bikeRacksTextView.text = amenities["bikeRacks"].toString()
        } else {
            bikeRacksLinearLayout.visibility = View.GONE
        }

        if (amenities.contains("taxiService")) {
            taxiLinearLayout.visibility = View.VISIBLE
            val taxiTextView = taxiLinearLayout.findViewById<TextView>(R.id.taxiTextView)
            taxiTextView.text = amenities["taxiService"].toString()
        } else {
            taxiLinearLayout.visibility = View.GONE
        }

        if (amenities.contains("parking")) {
            parkingLinearLayout.visibility = View.VISIBLE
            val parkingTextView = parkingLinearLayout.findViewById<TextView>(R.id.parkingTextView)
            parkingTextView.text = amenities["parking"].toString()
        } else {
            parkingLinearLayout.visibility = View.GONE
        }

        val stationHoursLinearLayout = view.findViewById<LinearLayout>(R.id.stationHoursLinearLayout)
        val stationHoursTextViewBody = view.findViewById<TextView>(R.id.stationHoursTextViewBody)
        val stationHours = stationDetails.get("hours") as String
        if (stationHours.isNotEmpty()) {
            stationHoursLinearLayout.visibility = View.VISIBLE
            stationHoursTextViewBody.text = stationHours
        } else {
            stationHoursLinearLayout.visibility = View.GONE
        }

        val faresLinearLayout = view.findViewById<LinearLayout>(R.id.faresLinearLayout)
        val faresTextViewBody = view.findViewById<TextView>(R.id.faresTextViewBody)
        faresTextViewBody.text = "Not yet implemented..."

        val gatedParkingLinearLayout = view.findViewById<LinearLayout>(R.id.gatedParkingLinearLayout)
        val gatedParkingTextViewBody = view.findViewById<TextView>(R.id.gatedParkingTextViewBody)
        val gatedParking = stationDetails.get("gatedParking") as String
        if (gatedParking.isNotEmpty()) {
            gatedParkingLinearLayout.visibility = View.VISIBLE
            gatedParkingTextViewBody.text = gatedParking
        } else {
            gatedParkingLinearLayout.visibility = View.GONE
        }

        val walkingDistanceLinearLayout = view.findViewById<LinearLayout>(R.id.walkingDistanceLinearLayout)
        val walkingDistanceTextViewBody = view.findViewById<TextView>(R.id.walkingDistanceTextViewBody)
        val walkingDistance = stationDetails.get("walkingDistance") as String
        if (walkingDistance.isNotEmpty()) {
            walkingDistanceLinearLayout.visibility = View.VISIBLE
            walkingDistanceTextViewBody.text = walkingDistance
        } else {
            walkingDistanceLinearLayout.visibility = View.GONE
        }
    }

}