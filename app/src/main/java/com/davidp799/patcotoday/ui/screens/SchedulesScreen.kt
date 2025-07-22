package com.davidp799.patcotoday.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
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

    // Animate shimmer fade in/out
    val shimmerAlpha by animateFloatAsState(
        targetValue = if (uiState.isLoading) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "shimmer_fade"
    )

    // Animate content fade in/out
    val contentAlpha by animateFloatAsState(
        targetValue = if (!uiState.isLoading && uiState.arrivals.isNotEmpty()) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = if (!uiState.isLoading) 150 else 0, // Slight delay when fading in content
            easing = FastOutSlowInEasing
        ),
        label = "content_fade"
    )

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

        // Schedule list with animated shimmer loading
        Box(modifier = Modifier.fillMaxSize()) {
            // Shimmer loading state
            if (uiState.isLoading || shimmerAlpha > 0f) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(18) { index ->
                        ScheduleItemShimmer(alpha = shimmerAlpha)
                        if (index < 17) { // Don't add divider after last item
                            HorizontalDivider(
                                modifier = Modifier.alpha(shimmerAlpha),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Actual content
            if (!uiState.isLoading || contentAlpha > 0f) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .alpha(contentAlpha),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    itemsIndexed(uiState.arrivals) { index, arrival ->
                        ScheduleItem(
                            arrival = arrival,
                            isHighlighted = index == uiState.scrollToIndex,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (index < uiState.arrivals.size - 1) { // Don't add divider after last item
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SchedulesScreenPreview() {
    MaterialTheme {
        SchedulesScreen()
    }
}
