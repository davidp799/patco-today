package com.davidp799.patcotoday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidp799.patcotoday.ui.screens.components.StationDetailsSheet
import com.davidp799.patcotoday.ui.screens.components.StationListItem
import com.davidp799.patcotoday.ui.screens.viewmodels.StationMapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationMapScreen() {
    val viewModel: StationMapViewModel = viewModel()
    val selectedStation by viewModel.selectedStation

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(viewModel.stations) { index, station ->
                StationListItem(
                    station = station,
                    position = index,
                    totalStations = viewModel.stations.size,
                    onStationClick = { viewModel.selectStation(it) }
                )
            }
        }
    }

    // Show bottom sheet when a station is selected
    selectedStation?.let { station ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelection() }
        ) {
            StationDetailsSheet(
                station = station,
                onDismiss = { viewModel.clearSelection() }
            )
        }
    }
}
