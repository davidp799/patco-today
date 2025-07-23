package com.davidp799.patcotoday

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.davidp799.patcotoday.data.repository.ScheduleRepository
import com.davidp799.patcotoday.ui.components.BottomNavigationBar
import com.davidp799.patcotoday.ui.components.TopNavigationBar
import com.davidp799.patcotoday.ui.navigation.Navigation
import com.davidp799.patcotoday.ui.screens.SchedulesScreenViewModel
import com.davidp799.patcotoday.ui.theme.PatcoTodayTheme
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MainActivity : ComponentActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var scheduleRepository: ScheduleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get theme preferences to configure system bars
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = prefs.getString("device_theme", "3")?.toInt() ?: 3
        val isSystemInDarkTheme = resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

        // Determine if we should use dark theme
        val useDarkTheme = when (themePreference) {
            1 -> false // Light theme
            2 -> true  // Dark theme
            3 -> isSystemInDarkTheme // Follow system
            else -> isSystemInDarkTheme
        }

        // Configure edge-to-edge with proper system bar styles
        enableEdgeToEdge(
            statusBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(android.graphics.Color.BLACK)
            } else {
                SystemBarStyle.light(android.graphics.Color.WHITE, android.graphics.Color.BLACK)
            },
            navigationBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(android.graphics.Color.BLACK)
            } else {
                SystemBarStyle.light(android.graphics.Color.WHITE, android.graphics.Color.BLACK)
            }
        )

        // Initialize repository
        scheduleRepository = ScheduleRepository(this)

        // Register preference change listener
        prefs.registerOnSharedPreferenceChangeListener(this)

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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "device_theme", "dynamic_colors" -> {
                // Recreate activity to apply theme changes
                recreate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
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

    // Animate blur effect when refreshing schedules
    val blurRadius by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (currentRoute == "schedules" && schedulesUiState.isRefreshing) 8f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "main_blur_effect"
    )

    // Animate overlay alpha when refreshing schedules
    val overlayAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (currentRoute == "schedules" && schedulesUiState.isRefreshing) 0.3f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "main_overlay_fade"
    )

    Box(modifier = Modifier.fillMaxSize()) {
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
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    onSchedulesReselected = { schedulesViewModel.onSchedulesScreenReselected() }
                )
            },
            modifier = Modifier.blur(radius = blurRadius.dp)
        ) { innerPadding ->
            Navigation(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                schedulesViewModel = if (currentRoute == "schedules") schedulesViewModel else null
            )
        }

        // Blur overlay when refreshing schedules
        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = overlayAlpha)
                    )
            )
        }

        // Loading indicator on top of blur
        if (schedulesUiState.isRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PatcoTodayTheme {
        MainScreen()
    }
}