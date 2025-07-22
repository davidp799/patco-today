package com.davidp799.patcotoday.ui.screens

import android.app.Application
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
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            repository.fetchAndUpdateSchedules()
                .onSuccess { apiResponse ->
                    // For now, we'll generate mock data based on the current stations
                    // TODO: Parse actual CSV files and generate real schedule data
                    val mockArrivals = generateMockArrivals()
                    val hasSpecial = apiResponse.specialSchedules != null

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        arrivals = mockArrivals,
                        hasSpecialSchedule = hasSpecial,
                        scrollToIndex = findNextArrival(mockArrivals)
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message,
                        arrivals = generateMockArrivals() // Fallback to mock data
                    )
                }
        }
    }

    fun updateFromStation(station: String) {
        _uiState.value = _uiState.value.copy(fromStation = station)
        // Reload schedule data when stations change
        loadScheduleData()
    }

    fun updateToStation(station: String) {
        _uiState.value = _uiState.value.copy(toStation = station)
        // Reload schedule data when stations change
        loadScheduleData()
    }

    fun reverseStations() {
        val currentFrom = _uiState.value.fromStation
        val currentTo = _uiState.value.toStation
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
            val timeString = String.format("%02d:%02d", hour, minute)
            val travelTime = "${(35..50).random()} min"

            arrivals.add(Arrival(timeString, travelTime))
        }

        return arrivals
    }

    private fun findNextArrival(arrivals: List<Arrival>): Int {
        // Simple logic to find the next upcoming arrival
        // TODO: Implement proper time comparison logic
        return (arrivals.size * 0.3).toInt() // Mock: scroll to about 30% down the list
    }
}
