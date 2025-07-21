package com.davidp799.patcotoday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.davidp799.patcotoday.ui.components.BottomNavigationBar
import com.davidp799.patcotoday.ui.components.TopNavigationBar
import com.davidp799.patcotoday.ui.navigation.Navigation
import com.davidp799.patcotoday.ui.theme.PatcoTodayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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