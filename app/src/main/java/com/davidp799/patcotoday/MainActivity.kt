package com.davidp799.patcotoday

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.davidp799.patcotoday.data.repository.ScheduleRepository
import com.davidp799.patcotoday.ui.components.BottomNavigationBar
import com.davidp799.patcotoday.ui.components.TopNavigationBar
import com.davidp799.patcotoday.ui.navigation.Navigation
import com.davidp799.patcotoday.ui.screens.SchedulesScreenViewModel
import com.davidp799.patcotoday.ui.theme.PatcoTodayTheme
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MainActivity : ComponentActivity() {

    private lateinit var scheduleRepository: ScheduleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize repository
        scheduleRepository = ScheduleRepository(this)

        // Make API call once on app start
        lifecycleScope.launch {
            Log.d("[ApiDebug]", "MainActivity: Starting initial API call on app startup")
            scheduleRepository.fetchAndUpdateSchedules()
                .onSuccess { apiResponse ->
                    Log.d("[ApiDebug]", "MainActivity: Initial API call successful, schedules updated")

                    // Check for special schedules
                    if (apiResponse.specialSchedules != null) {
                        showToast("Special schedules available")
                    }

                    // Check if regular schedules were updated
                    val regularSchedules = apiResponse.regularSchedules
                    if (regularSchedules != null) {
                        if (regularSchedules.updated) {
                            showToast("Schedule data updated successfully")
                        }
                    } else {
                        showToast("Schedule data loaded from cache")
                    }
                }
                .onFailure { error ->
                    Log.e("[ApiDebug]", "MainActivity: Initial API call failed: ${error.message}")

                    // Show appropriate error message based on error type
                    val errorMessage = when (error) {
                        is UnknownHostException -> {
                            "No internet connection. Using cached schedules."
                        }
                        is SocketTimeoutException -> {
                            "Request timed out. Using cached schedules."
                        }
                        else -> {
                            if (error.message?.contains("404") == true) {
                                "Schedule service unavailable. Using cached schedules."
                            } else if (error.message?.contains("500") == true) {
                                "Server error. Using cached schedules."
                            } else {
                                "Failed to update schedules. Using cached data."
                            }
                        }
                    }

                    showToast(errorMessage)
                }
        }

        setContent {
            PatcoTodayTheme {
                MainScreen()
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Create ViewModel instance once and reuse it
    val schedulesViewModel: SchedulesScreenViewModel = viewModel()
    val schedulesUiState by schedulesViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopNavigationBar(
                navController = navController,
                onRefreshClick = if (currentRoute == "schedules") {
                    { schedulesViewModel.refreshSchedules() }
                } else null,
                isRefreshing = schedulesUiState.isRefreshing
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) },
        modifier = Modifier
    ) { innerPadding ->
        Navigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            schedulesViewModel = if (currentRoute == "schedules") schedulesViewModel else null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PatcoTodayTheme {
        MainScreen()
    }
}