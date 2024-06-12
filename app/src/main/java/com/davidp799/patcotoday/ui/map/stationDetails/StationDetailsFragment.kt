package com.davidp799.patcotoday.ui.map.stationDetails

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.davidp799.patcotoday.R
import com.davidp799.patcotoday.databinding.FragmentStationDetailsBinding
import com.google.android.material.transition.MaterialFadeThrough

class StationDetailsFragment : Fragment() {
    private var _binding: FragmentStationDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val stationName = arguments?.getString("stationName")
        val stationDetailsViewModel =
            ViewModelProvider(this)[StationDetailsViewModel::class.java]
        _binding = FragmentStationDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setLayout(root, stationDetailsViewModel.stationDetailsList[stationName])

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()

        return root
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
        var stationHours = stationDetails["hours"] as String
        if (stationHours.isNotEmpty()) {
            stationHoursLinearLayout.visibility = View.VISIBLE
            stationHoursTextViewBody.text = stationHours
        } else {
            stationHours = "Station is open 24/7."
            stationHoursTextViewBody.text = stationHours
        }

        val gatedParkingLinearLayout = view.findViewById<LinearLayout>(R.id.gatedParkingLinearLayout)
        val gatedParkingTextViewBody = view.findViewById<TextView>(R.id.gatedParkingTextViewBody)
        val gatedParking = stationDetails["gatedParking"] as String
        if (gatedParking.isNotEmpty()) {
            gatedParkingLinearLayout.visibility = View.VISIBLE
            gatedParkingTextViewBody.text = gatedParking
        } else {
            gatedParkingLinearLayout.visibility = View.GONE
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