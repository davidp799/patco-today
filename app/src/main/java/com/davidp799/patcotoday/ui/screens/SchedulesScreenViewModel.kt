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

enum class SpecialScheduleState {
    NONE,           // No special schedules found for today
    AVAILABLE,      // Special schedules are available
    NETWORK_ERROR,  // Failed to check for special schedules due to network issues
    LOADING         // Currently checking for special schedules
}

data class SchedulesUiState(
    val isLoading: Boolean = true,
    val arrivals: List<Arrival> = emptyList(),
    val fromStation: String = "Lindenwold",
    val toStation: String = "15–16th & Locust",
    val scrollToIndex: Int = 0,
    val errorMessage: String? = null,
    val hasSpecialSchedule: Boolean = false,
    val specialScheduleState: SpecialScheduleState = SpecialScheduleState.LOADING,
    val isRefreshing: Boolean = false,
    val lastRefreshTime: Long = 0L,
    val isSpamming: Boolean = false,
    val lastClickTime: Long = 0L,
    val spamModeStartTime: Long = 0L,
    val showSpecialScheduleSheet: Boolean = false,
    val hasUserDismissedSheet: Boolean = false
)

class SchedulesScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(application)

    private val _uiState = MutableStateFlow(SchedulesUiState())
    val uiState: StateFlow<SchedulesUiState> = _uiState.asStateFlow()

    // Callback for showing toast messages
    private var showToastCallback: ((String) -> Unit)? = null

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
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Only get schedule data from local storage, no API call
            val arrivals = repository.getScheduleForRoute(
                fromStation = _uiState.value.fromStation,
                toStation = _uiState.value.toStation
            )

            // Check special schedule status
            val specialScheduleState = checkSpecialScheduleStatus()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                arrivals = arrivals,
                hasSpecialSchedule = specialScheduleState == SpecialScheduleState.AVAILABLE,
                specialScheduleState = specialScheduleState,
                showSpecialScheduleSheet = !_uiState.value.hasUserDismissedSheet,
                scrollToIndex = findNextArrival(arrivals),
                errorMessage = if (arrivals.isEmpty()) "No schedule data available" else null
            )
        }
    }

    private fun checkSpecialScheduleStatus(): SpecialScheduleState {
        return try {
            val context = getApplication<Application>()
            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

            // Check if special schedule files exist for today
            val specialDir = java.io.File(context.filesDir, "schedules/special/$currentDate")
            val pdfFile = java.io.File(specialDir, "special_schedule.pdf")
            val eastboundFile = java.io.File(specialDir, "special_schedule_eastbound.csv")
            val westboundFile = java.io.File(specialDir, "special_schedule_westbound.csv")

            val hasSpecialFiles = pdfFile.exists() || eastboundFile.exists() || westboundFile.exists()

            if (hasSpecialFiles) {
                SpecialScheduleState.AVAILABLE
            } else {
                SpecialScheduleState.NONE
            }
        } catch (e: Exception) {
            Log.e("[checkSpecialScheduleStatus]", "Error checking special schedule status: ${e.message}")
            SpecialScheduleState.NETWORK_ERROR
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

    fun refreshSchedules() {
        val currentTime = System.currentTimeMillis()
        val currentState = _uiState.value

        // Check if we're currently in spam mode and if enough time has passed to exit
        if (currentState.isSpamming) {
            val timeSinceSpamStart = currentTime - currentState.spamModeStartTime
            if (timeSinceSpamStart >= 5000L) {
                _uiState.value = currentState.copy(
                    isSpamming = false,
                    lastClickTime = currentTime
                )
                // Continue with normal refresh logic below
            } else {
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
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)

            try {
                // Make API call to fetch and update schedules
                val result = repository.fetchAndUpdateSchedules()

                result.onSuccess {
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
                    Log.e("[refreshSchedules]", "Manual refresh failed: ${error.message}")

                    // Determine the appropriate error message based on the failure reason
                    val context = getApplication<Application>()
                    val errorMessage = when {
                        // Check if failure is due to mobile data restrictions
                        com.davidp799.patcotoday.utils.NetworkUtils.isOnMobileData(context) &&
                        !com.davidp799.patcotoday.utils.NetworkUtils.isDownloadOnMobileDataEnabled(context) -> {
                            ""
                        }
                        // Other network-related errors
                        error is java.net.UnknownHostException -> {
                            "No internet connection. Please check your network and try again."
                        }
                        error is java.net.SocketTimeoutException -> {
                            "Request timed out. Please try again later."
                        }
                        error.message?.contains("404") == true -> {
                            "Schedule service temporarily unavailable. Please try again later."
                        }
                        error.message?.contains("500") == true -> {
                            "Server error occurred. Please try again later."
                        }
                        else -> {
                            "Failed to update schedules. Please try again later."
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        errorMessage = "Failed to refresh schedules: ${error.message}"
                    )

                    // Show specific toast message
                    if (errorMessage.isNotEmpty()) showToastCallback?.invoke(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("[FirstRunDebug]", "Exception during manual refresh: ${e.message}", e)

                // Determine the appropriate error message for exceptions
                val context = getApplication<Application>()
                val errorMessage = when {
                    // Check if exception is due to mobile data restrictions
                    com.davidp799.patcotoday.utils.NetworkUtils.isOnMobileData(context) &&
                    !com.davidp799.patcotoday.utils.NetworkUtils.isDownloadOnMobileDataEnabled(context) -> {
                        "Download on mobile data is disabled. Enable it in settings or connect to Wi-Fi to refresh schedules."
                    }
                    else -> {
                        "Failed to update schedules. Please try again later."
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = "Failed to refresh schedules"
                )

                // Show specific toast message
                showToastCallback?.invoke(errorMessage)
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

    fun dismissSpecialScheduleSheet() {
        _uiState.value = _uiState.value.copy(
            showSpecialScheduleSheet = false,
            hasUserDismissedSheet = true
        )
    }

    fun openSpecialSchedulePdf() {
        val context = getApplication<Application>()
        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val pdfFile = java.io.File(context.filesDir, "schedules/special/$currentDate/special_schedule.pdf")

        if (pdfFile.exists()) {
            try {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )

                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("[FirstRunDebug]", "Failed to open PDF: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to open schedule PDF"
                )
            }
        } else {
            Log.e("[FirstRunDebug]", "PDF file not found: ${pdfFile.absolutePath}")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Schedule PDF not found"
            )
        }
    }

    // Call this when user navigates to schedules screen
    fun onSchedulesScreenReselected() {
        val currentState = _uiState.value
        if (currentState.hasUserDismissedSheet &&
            (currentState.specialScheduleState == SpecialScheduleState.AVAILABLE ||
             currentState.specialScheduleState == SpecialScheduleState.NONE ||
             currentState.specialScheduleState == SpecialScheduleState.NETWORK_ERROR)) {
            // Bring back the sheet in peeking state for any special schedule state, not dismissed
            _uiState.value = currentState.copy(
                showSpecialScheduleSheet = true,
                hasUserDismissedSheet = false
            )
        }
    }

    // Set the callback for showing toast messages
    fun setShowToastCallback(callback: (String) -> Unit) {
        showToastCallback = callback
    }
}
