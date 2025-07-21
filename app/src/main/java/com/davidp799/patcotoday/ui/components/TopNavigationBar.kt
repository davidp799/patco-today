package com.davidp799.patcotoday.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.davidp799.patcotoday.ui.navigation.bottomNavItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Find the current screen title
    val currentScreen = bottomNavItems.find { it.route == currentRoute }
    val title = currentScreen?.title ?: ""

    CenterAlignedTopAppBar(
        title = { Text(title) },
        actions = {
            IconButton(onClick = {
                navController.navigate("settings")
            }) {
                Icon(
                    imageVector = Icons.TwoTone.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}
