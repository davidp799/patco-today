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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
import com.davidp799.patcotoday.utils.NetworkUtils
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var scheduleRepository: ScheduleRepository
    private var isFirstRunComplete = mutableStateOf(false)
    private var firstRunLoadingMessage = mutableStateOf("Loading schedules...")

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash screen visible until app is ready (but not during first launch loading screen)
        splashScreen.setKeepOnScreenCondition {
            !isFirstRunComplete.value
        }

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

        // Handle first run logic completely before showing main UI
        lifecycleScope.launch {
            handleFirstRunAndDataLoading()
        }

        setContent {
            PatcoTodayTheme {
                if (isFirstRunComplete.value) {
                    MainScreen()
                } else {
                    FirstRunLoadingScreen(loadingMessage = firstRunLoadingMessage.value)
                }
            }
        }
    }

    private suspend fun handleFirstRunAndDataLoading() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isFirstRun = !prefs.getBoolean("first_run_completed", false)

        if (isFirstRun) {
            // Show appropriate loading message for first run
            firstRunLoadingMessage.value = "Downloading latest schedules..."

            // Perform first run API call and wait for completion
            scheduleRepository.fetchAndUpdateSchedules()
                .onSuccess { apiResponse ->
                    // Mark first run as completed
                    prefs.edit().putBoolean("first_run_completed", true).apply()

                    // Check if regular schedules were updated
                    val regularSchedules = apiResponse.regularSchedules
                    if (regularSchedules != null) {
                        if (regularSchedules.updated) {
                            showToast("Schedule data downloaded successfully")
                        }
                    } else {
                        showToast("Schedule data loaded from cache")
                    }

                    // Request app review after successful data load
                    requestReview()
                }
                .onFailure { error ->
                    Log.e("[checkIfFirstRun]", "First run API call failed: ${error.message}")

                    // Mark first run as completed anyway so UI can proceed with fallback data
                    prefs.edit()
                        .putBoolean("first_run_completed", true)
                        .putBoolean("using_fallback_data", true)
                        .apply()

                    // Show appropriate error message based on error type
                    val errorMessage = when (error) {
                        is UnknownHostException -> {
                            "No internet connection. Using offline schedules."
                        }
                        is SocketTimeoutException -> {
                            "Request timed out. Using offline schedules."
                        }
                        else -> {
                            if (error.message?.contains("404") == true) {
                                "Schedule service unavailable. Using offline schedules."
                            } else if (error.message?.contains("500") == true) {
                                "Server error. Using offline schedules."
                            } else {
                                if (NetworkUtils.isOnMobileData(this@MainActivity) &&
                                    !NetworkUtils.isDownloadOnMobileDataEnabled(this@MainActivity)) {
                                    "Download on mobile data disabled. Using offline schedules."
                                } else {
                                    "Failed to download schedules. Using offline data."
                                }
                            }
                        }
                    }
                    if (errorMessage.isNotEmpty()) showToast(errorMessage)

                    // Request app review even on API failure to track visits
                    requestReview()
                }
        } else {
            // Not first run, still make API call but don't block UI
            lifecycleScope.launch {
                scheduleRepository.fetchAndUpdateSchedules()
                    .onSuccess { apiResponse ->
                        // Check if regular schedules were updated
                        val regularSchedules = apiResponse.regularSchedules
                        if (regularSchedules != null) {
                            if (regularSchedules.updated) {
                                showToast("Schedule data updated successfully")
                            }
                        }
                        requestReview()
                    }
                    .onFailure { error ->
                        Log.e("[MainActivity]", "Background API call failed: ${error.message}")
                        // Don't show error messages for background updates unless critical
                        requestReview()
                    }
            }
        }

        // Mark first run as complete so UI can be shown
        isFirstRunComplete.value = true
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
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Request Google Play Store app review
    private fun requestReview() {
        val prefVisitNumber = "visit_number"
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val visitNumber = prefs.getInt(prefVisitNumber, 0)
        val sharedPreferencesEditor = prefs.edit()

        if (visitNumber % 10 == 0) {
            val reviewManager = ReviewManagerFactory.create(this)
            val requestReviewFlow = reviewManager.requestReviewFlow()
            requestReviewFlow.addOnCompleteListener {
                if (it.isSuccessful) {
                    val reviewInfo = it.result
                    val reviewFlow = reviewManager.launchReviewFlow(this, reviewInfo)
                    reviewFlow.addOnCompleteListener {
                        sharedPreferencesEditor.putInt(prefVisitNumber, visitNumber + 1).apply()
                    }
                } else {
                    @ReviewErrorCode val reviewErrorCode = (it.exception as? ReviewException)?.errorCode
                    Log.e(
                        "[requestReview]",
                        "reviewErrorCode = $reviewErrorCode"
                    )
                    // Still increment visit number even if review request failed
                    sharedPreferencesEditor.putInt(prefVisitNumber, visitNumber + 1).apply()
                }
            }
        } else {
            sharedPreferencesEditor.putInt(prefVisitNumber, visitNumber + 1).apply()
        }
    }
}

@Composable
fun FirstRunLoadingScreen(loadingMessage: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = loadingMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
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

    // Set up toast callback for the ViewModel
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        schedulesViewModel.setShowToastCallback { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Animate blur effect when refreshing schedules
    val blurRadius by animateFloatAsState(
        targetValue = if (currentRoute == "schedules" && schedulesUiState.isRefreshing) 8f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "main_blur_effect"
    )

    // Animate overlay alpha when refreshing schedules
    val overlayAlpha by animateFloatAsState(
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