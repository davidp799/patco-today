package com.davidp799.patcotoday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.davidp799.patcotoday.ui.screens.FeedbackScreen
import com.davidp799.patcotoday.ui.theme.PatcoTodayTheme

class FeedbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PatcoTodayTheme {
                FeedbackScreen(
                    onNavigateUp = {
                        finish() // Simply finish this activity to go "back"
                    }
                )
            }
        }
    }
}