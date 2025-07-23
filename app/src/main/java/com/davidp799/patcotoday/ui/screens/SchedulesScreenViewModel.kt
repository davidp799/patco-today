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
    val hasSpecialSchedule: Boolean = false,
    val isRefreshing: Boolean = false,
    val lastRefreshTime: Long = 0L,
    val isSpamming: Boolean = false,
    val lastClickTime: Long = 0L,
    val spamModeStartTime: Long = 0L
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

    private fun loadScheduleData() {
        viewModelScope.launch {
            Log.d("[ApiDebug]", "Starting to load schedule data for route: ${_uiState.value.fromStation} -> ${_uiState.value.toStation}")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Only get schedule data from local storage, no API call
            val arrivals = repository.getScheduleForRoute(
                fromStation = _uiState.value.fromStation,
                toStation = _uiState.value.toStation
            )

            Log.d("[ApiDebug]", "Retrieved ${arrivals.size} arrivals from local storage")

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                arrivals = arrivals,
                hasSpecialSchedule = false, // Will be determined by the CSV parser
                scrollToIndex = findNextArrival(arrivals),
                errorMessage = if (arrivals.isEmpty()) "No schedule data available" else null
            )
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

    fun refreshSchedules() {
        val currentTime = System.currentTimeMillis()
        val currentState = _uiState.value

        // Check if we're currently in spam mode and if enough time has passed to exit
        if (currentState.isSpamming) {
            val timeSinceSpamStart = currentTime - currentState.spamModeStartTime
            if (timeSinceSpamStart >= 5000L) {
                Log.d("[ApiDebug]", "Spam mode timeout reached - exiting spam mode")
                _uiState.value = currentState.copy(
                    isSpamming = false,
                    lastClickTime = currentTime
                )
                // Continue with normal refresh logic below
            } else {
                Log.d("[ApiDebug]", "Still in spam mode - performing fake refresh")
                _uiState.value = currentState.copy(isRefreshing = true)

                // Fake loading time (1-2 seconds)
                viewModelScope.launch {
                    kotlinx.coroutines.delay((1000..2000).random().toLong())
                    _uiState.value = _uiState.value.copy(isRefreshing = false)
                }
                return
            }
        }

        val timeSinceLastClick = currentTime - currentState.lastClickTime

        // Check if user is spam clicking (more than 3 clicks in 3 seconds)
        val isSpamClick = timeSinceLastClick < 1000L && currentState.lastClickTime > 0

        // Update click time
        _uiState.value = currentState.copy(lastClickTime = currentTime)

        // If spam clicking detected, enter spam mode
        if (isSpamClick && !currentState.isSpamming) {
            Log.d("[ApiDebug]", "Spam clicking detected - entering spam mode for 5 seconds")
            _uiState.value = _uiState.value.copy(
                isSpamming = true,
                spamModeStartTime = currentTime,
                isRefreshing = true
            )

            // Fake loading time and return
            viewModelScope.launch {
                kotlinx.coroutines.delay((1000..2000).random().toLong())
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
            return
        }

        // Normal refresh logic
        viewModelScope.launch {
            Log.d("[ApiDebug]", "Performing real refresh")
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)

            try {
                // Make API call to fetch and update schedules
                val result = repository.fetchAndUpdateSchedules()

                result.onSuccess {
                    Log.d("[ApiDebug]", "Manual refresh successful, reloading schedule data")

                    // After API call, reload the schedule data from local storage
                    val arrivals = repository.getScheduleForRoute(
                        fromStation = _uiState.value.fromStation,
                        toStation = _uiState.value.toStation
                    )

                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        arrivals = arrivals,
                        scrollToIndex = findNextArrival(arrivals),
                        errorMessage = if (arrivals.isEmpty()) "No schedule data available" else null,
                        lastRefreshTime = System.currentTimeMillis()
                    )
                }.onFailure { error ->
                    Log.e("[ApiDebug]", "Manual refresh failed: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        errorMessage = "Failed to refresh schedules: ${error.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e("[ApiDebug]", "Exception during manual refresh: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = "Failed to refresh schedules"
                )
            }
        }
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
