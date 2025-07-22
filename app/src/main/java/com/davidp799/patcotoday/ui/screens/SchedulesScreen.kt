package com.davidp799.patcotoday.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.davidp799.patcotoday.ui.components.TripConfigurationBar

@Composable
fun SchedulesScreen() {
    var fromStation by remember { mutableStateOf("Lindenwold") }
    var toStation by remember { mutableStateOf("15-16th & Locust") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TripConfigurationBar(
            fromStation = fromStation,
            toStation = toStation,
            onFromStationChange = { fromStation = it },
            onToStationChange = { toStation = it },
            onReverseStationsClick = {
                val temp = fromStation
                fromStation = toStation
                toStation = temp
            }
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Schedules Screen")
        }
    }
}

