package com.davidp799.patcotoday.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material.icons.twotone.Map
import androidx.compose.material.icons.twotone.Train
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Schedules : Screen("schedules", "Schedules", Icons.TwoTone.Train)
    data object StationMap : Screen("station_map", "Station Map", Icons.TwoTone.Map)
    data object Information : Screen("information", "Information", Icons.TwoTone.Info)
}

val bottomNavItems = listOf(
    Screen.Schedules,
    Screen.StationMap,
    Screen.Information
)

