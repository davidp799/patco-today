package com.davidp799.patcotoday.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davidp799.patcotoday.data.repository.ScheduleRepository
import com.davidp799.patcotoday.utils.Arrival
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SchedulesUiState(
    val isLoading: Boolean = true,
    val arrivals: List<Arrival> = emptyList(),
    val fromStation: String = "Lindenwold",
    val toStation: String = "15–16th & Locust",
    val scrollToIndex: Int = 0,
    val errorMessage: String? = null,
    val hasSpecialSchedule: Boolean = false
)

class SchedulesScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(application)

    private val _uiState = MutableStateFlow(SchedulesUiState())
    val uiState: StateFlow<SchedulesUiState> = _uiState.asStateFlow()

    val stationOptions = listOf(
        "Lindenwold",
        "Ashland",
        "Woodcrest",
        "Haddonfield",
        "Westmont",
        "Collingswood",
        "Ferry Avenue",
        "Broadway",
        "City Hall",
        "Franklin Square",
        "8th & Market",
        "9–10th & Locust",
        "12–13th & Locust",
        "15–16th & Locust"
    )

    init {
        loadScheduleData()
    }

    fun loadScheduleData() {
        viewModelScope.launch {
            Log.d("[ApiDebug]", "Starting to load schedule data for route: ${_uiState.value.fromStation} -> ${_uiState.value.toStation}")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            repository.fetchAndUpdateSchedules()
                .onSuccess { apiResponse ->
                    Log.d("[ApiDebug]", "API call successful, getting arrival times from repository")
                    // Get real schedule data using CSV parser
                    val realArrivals = repository.getScheduleForRoute(
                        fromStation = _uiState.value.fromStation,
                        toStation = _uiState.value.toStation
                    )

                    val hasSpecial = apiResponse.specialSchedules != null
                    Log.d("[ApiDebug]", "Retrieved ${realArrivals.size} arrivals, has special schedule: $hasSpecial")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        arrivals = realArrivals,
                        hasSpecialSchedule = hasSpecial,
                        scrollToIndex = findNextArrival(realArrivals)
                    )
                }
                .onFailure { error ->
                    Log.e("[ApiDebug]", "API call failed: ${error.message}, trying to get cached arrivals")
                    // Try to get cached schedule data even if API fails
                    val cachedArrivals = repository.getScheduleForRoute(
                        fromStation = _uiState.value.fromStation,
                        toStation = _uiState.value.toStation
                    )

                    Log.d("[ApiDebug]", "Retrieved ${cachedArrivals.size} cached arrivals")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message,
                        arrivals = cachedArrivals
                    )
                }
        }
    }

    fun updateFromStation(station: String) {
        Log.d("[ApiDebug]", "Updating from station: ${_uiState.value.fromStation} -> $station")
        _uiState.value = _uiState.value.copy(fromStation = station)
        // Reload schedule data when stations change
        loadScheduleData()
    }

    fun updateToStation(station: String) {
        Log.d("[ApiDebug]", "Updating to station: ${_uiState.value.toStation} -> $station")
        _uiState.value = _uiState.value.copy(toStation = station)
        // Reload schedule data when stations change
        loadScheduleData()
    }

    fun reverseStations() {
        val currentFrom = _uiState.value.fromStation
        val currentTo = _uiState.value.toStation
        Log.d("[ApiDebug]", "Reversing stations: $currentFrom <-> $currentTo")

        _uiState.value = _uiState.value.copy(
            fromStation = currentTo,
            toStation = currentFrom
        )
        // Reload schedule data when stations change
        loadScheduleData()
    }

    private fun generateMockArrivals(): List<Arrival> {
        // Generate mock schedule data for now
        // TODO: Replace with actual CSV parsing logic
        val arrivals = mutableListOf<Arrival>()
        val baseHour = 6

        for (i in 0..23) {
            val hour = (baseHour + i) % 24
            val minute = listOf(15, 45).random()

            // Calculate destination time (add 30-50 minutes to source time)
            val travelMinutes = (30..50).random()
            val destinationTotalMinutes = (hour * 60 + minute + travelMinutes) % (24 * 60)
            val destinationHour = destinationTotalMinutes / 60
            val destinationMinute = destinationTotalMinutes % 60

            // Format source time
            val sourceAmPm = if (hour < 12) "AM" else "PM"
            val sourceDisplayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val sourceTimeString = String.format("%d:%02d %s", sourceDisplayHour, minute, sourceAmPm)

            // Format destination time
            val destAmPm = if (destinationHour < 12) "AM" else "PM"
            val destDisplayHour = if (destinationHour == 0) 12 else if (destinationHour > 12) destinationHour - 12 else destinationHour
            val destTimeString = String.format("%d:%02d %s", destDisplayHour, destinationMinute, destAmPm)

            arrivals.add(Arrival(sourceTimeString, destTimeString))
        }

        return arrivals
    }

    private fun findNextArrival(arrivals: List<Arrival>): Int {
        if (arrivals.isEmpty()) return 0

        val currentTime = java.util.Calendar.getInstance()
        val currentHour = currentTime.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(java.util.Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        for (i in arrivals.indices) {
            try {
                val arrivalTime = arrivals[i].arrivalTime
                    .replace(" AM", "")
                    .replace(" PM", "")

                val parts = arrivalTime.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()

                // Convert to 24-hour format
                val hour24 = if (arrivals[i].arrivalTime.contains("PM") && hour != 12) {
                    hour + 12
                } else if (arrivals[i].arrivalTime.contains("AM") && hour == 12) {
                    0
                } else {
                    hour
                }

                val arrivalTotalMinutes = hour24 * 60 + minute

                // Return index of first arrival that's in the future
                if (arrivalTotalMinutes >= currentTotalMinutes) {
                    return i
                }
            } catch (e: Exception) {
                continue
            }
        }

        return 0 // Default to first item if no future arrivals found
    }
}
