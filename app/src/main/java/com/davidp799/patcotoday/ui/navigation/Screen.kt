package com.davidp799.patcotoday.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Schedules : Screen("schedules", "Schedules", Icons.Default.List)
    object StationMap : Screen("station_map", "Station Map", Icons.Default.Place)
    object Information : Screen("information", "Information", Icons.Default.Info)
}

val bottomNavItems = listOf(
    Screen.Schedules,
    Screen.StationMap,
    Screen.Information
)

