package com.davidp799.patcotoday.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Refresh
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.davidp799.patcotoday.ui.navigation.bottomNavItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    navController: NavController,
    onRefreshClick: (() -> Unit)? = null,
    isRefreshing: Boolean = false
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Find the current screen title
    val currentScreen = bottomNavItems.find { it.route == currentRoute }
    val title = currentScreen?.title ?: ""

    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            // Show refresh button only on schedules screen
            if (currentRoute == "schedules" && onRefreshClick != null) {
                IconButton(onClick = onRefreshClick) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.TwoTone.Refresh,
                            contentDescription = "Refresh Schedules"
                        )
                    }
                }
            }
        },
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

@Preview(showBackground = true)
@Composable
fun TopNavigationBarPreview() {
    MaterialTheme {
        TopNavigationBar(
            navController = rememberNavController(),
            onRefreshClick = { },
            isRefreshing = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopNavigationBarRefreshingPreview() {
    MaterialTheme {
        TopNavigationBar(
            navController = rememberNavController(),
            onRefreshClick = { },
            isRefreshing = true
        )
    }
}
