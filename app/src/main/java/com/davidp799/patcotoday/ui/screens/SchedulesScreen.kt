package com.davidp799.patcotoday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidp799.patcotoday.ui.components.TripConfigurationBar
import com.davidp799.patcotoday.ui.components.DropShadow
import com.davidp799.patcotoday.ui.components.ScheduleItem
import com.davidp799.patcotoday.ui.components.ScheduleItemShimmer

@Composable
fun SchedulesScreen(
    viewModel: SchedulesScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to next arrival when data loads
    LaunchedEffect(uiState.arrivals, uiState.scrollToIndex) {
        if (uiState.arrivals.isNotEmpty() && uiState.scrollToIndex > 0) {
            listState.animateScrollToItem(uiState.scrollToIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TripConfigurationBar(
            fromStation = uiState.fromStation,
            toStation = uiState.toStation,
            onFromStationChange = { viewModel.updateFromStation(it) },
            onToStationChange = { viewModel.updateToStation(it) },
            onReverseStationsClick = { viewModel.reverseStations() },
            stations = viewModel.stationOptions
        )

        DropShadow()

        // Schedule list with shimmer loading
        if (uiState.isLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(18) { // Show 18 shimmer items like the original
                    ScheduleItemShimmer()
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(uiState.arrivals) { index, arrival ->
                    ScheduleItem(
                        arrival = arrival,
                        isHighlighted = index == uiState.scrollToIndex,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
