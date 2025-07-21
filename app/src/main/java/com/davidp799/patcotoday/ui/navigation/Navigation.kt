package com.davidp799.patcotoday.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.davidp799.patcotoday.ui.screens.InformationScreen
import com.davidp799.patcotoday.ui.screens.SchedulesScreen
import com.davidp799.patcotoday.ui.screens.StationMapScreen

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Schedules.route) {
        composable(Screen.Schedules.route) {
            SchedulesScreen()
        }
        composable(Screen.StationMap.route) {
            StationMapScreen()
        }
        composable(Screen.Information.route) {
            InformationScreen()
        }
    }
}

