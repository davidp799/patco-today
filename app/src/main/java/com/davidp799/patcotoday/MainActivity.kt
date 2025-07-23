package com.davidp799.patcotoday

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.davidp799.patcotoday.data.repository.ScheduleRepository
import com.davidp799.patcotoday.ui.components.BottomNavigationBar
import com.davidp799.patcotoday.ui.components.TopNavigationBar
import com.davidp799.patcotoday.ui.navigation.Navigation
import com.davidp799.patcotoday.ui.theme.PatcoTodayTheme
import kotlinx.coroutines.launch

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
                }
                .onFailure { error ->
                    Log.e("[ApiDebug]", "MainActivity: Initial API call failed: ${error.message}")
                }
        }

        setContent {
            PatcoTodayTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        topBar = { TopNavigationBar(navController = navController) },
        bottomBar = { BottomNavigationBar(navController = navController) },
        modifier = Modifier
    ) { innerPadding ->
        Navigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
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