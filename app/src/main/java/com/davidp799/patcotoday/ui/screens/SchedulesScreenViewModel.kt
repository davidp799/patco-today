package com.davidp799.patcotoday.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.davidp799.patcotoday.utils.Arrival

data class SchedulesUiState(
    val fromStation: String = "Lindenwold",
    val toStation: String = "15-16th & Locust",
    val arrivals: List<Arrival> = emptyList(),
    val isLoading: Boolean = false,
    val scrollToIndex: Int = 0,
    val hasInternet: Boolean = true
)

class SchedulesScreenViewModel : ViewModel() {
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
        "8th and Market",
        "9-10th & Locust",
        "12-13th & Locust",
        "15-16th & Locust",
    )

    fun updateFromStation(station: String) {
        _uiState.value = _uiState.value.copy(fromStation = station)
        loadSchedules()
    }

    fun updateToStation(station: String) {
        _uiState.value = _uiState.value.copy(toStation = station)
        loadSchedules()
    }

    fun reverseStations() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            fromStation = currentState.toStation,
            toStation = currentState.fromStation
        )
        loadSchedules()
    }

    private fun loadSchedules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // TODO: Replace with actual load... Simulate loading delay
            kotlinx.coroutines.delay(1000)

            // Mock data for now - replace with actual schedule loading logic
            val mockArrivals = generateMockArrivals()
            val scrollIndex = findNextArrivalIndex(mockArrivals)

            _uiState.value = _uiState.value.copy(
                arrivals = mockArrivals,
                isLoading = false,
                scrollToIndex = scrollIndex
            )
        }
    }

    private fun generateMockArrivals(): List<Arrival> {
        // Generate mock schedule data - replace with actual data loading
        return listOf(
            Arrival("6:00 AM", "45 min"),
            Arrival("6:30 AM", "45 min"),
            Arrival("7:00 AM", "45 min"),
            Arrival("7:30 AM", "45 min"),
            Arrival("8:00 AM", "45 min"),
            Arrival("8:30 AM", "45 min"),
            Arrival("9:00 AM", "45 min"),
            Arrival("9:30 AM", "45 min"),
            Arrival("10:00 AM", "45 min"),
            Arrival("10:30 AM", "45 min"),
            Arrival("11:00 AM", "45 min"),
            Arrival("11:30 AM", "45 min"),
            Arrival("12:00 PM", "45 min"),
            Arrival("12:30 PM", "45 min"),
            Arrival("1:00 PM", "45 min"),
            Arrival("1:30 PM", "45 min"),
            Arrival("2:00 PM", "45 min"),
            Arrival("2:30 PM", "45 min")
        )
    }

    private fun findNextArrivalIndex(arrivals: List<Arrival>): Int {
        // Mock implementation - replace with actual time comparison logic
        Log.d("[SchedulesScreenViewModel]", "Arrivals: $arrivals")
        return 0
    }

    init {
        loadSchedules()
    }
}
