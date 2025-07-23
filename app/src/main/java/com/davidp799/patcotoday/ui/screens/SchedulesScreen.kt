package com.davidp799.patcotoday.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davidp799.patcotoday.ui.components.TripConfigurationBar
import com.davidp799.patcotoday.ui.components.ScheduleItem
import com.davidp799.patcotoday.ui.components.ScheduleItemShimmer
import com.davidp799.patcotoday.ui.components.SpecialScheduleBottomSheet
import com.davidp799.patcotoday.utils.Arrival

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    viewModel: SchedulesScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Bottom sheet state
    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = if (uiState.showSpecialScheduleSheet) SheetValue.PartiallyExpanded else SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    // Handle bottom sheet state changes
    LaunchedEffect(uiState.showSpecialScheduleSheet) {
        if (uiState.showSpecialScheduleSheet && !uiState.hasUserDismissedSheet) {
            // Show as peeking by default, not fully expanded
            bottomSheetState.bottomSheetState.partialExpand()
        } else if (!uiState.showSpecialScheduleSheet) {
            bottomSheetState.bottomSheetState.hide()
        }
    }

    // Handle when user manually dismisses sheet
    LaunchedEffect(bottomSheetState.bottomSheetState.targetValue) {
        if (bottomSheetState.bottomSheetState.targetValue == SheetValue.Hidden && uiState.showSpecialScheduleSheet) {
            viewModel.dismissSpecialScheduleSheet()
        }
    }

    // Function to determine if an arrival is in the past
    fun isArrivalInPast(arrival: Arrival): Boolean {
        return try {
            val currentTime = java.util.Calendar.getInstance()
            val currentHour = currentTime.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = currentTime.get(java.util.Calendar.MINUTE)
            val currentTotalMinutes = currentHour * 60 + currentMinute

            val arrivalTime = arrival.arrivalTime
                .replace(" AM", "")
                .replace(" PM", "")

            val parts = arrivalTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            // Convert to 24-hour format
            val hour24 = if (arrival.arrivalTime.contains("PM") && hour != 12) {
                hour + 12
            } else if (arrival.arrivalTime.contains("AM") && hour == 12) {
                0
            } else {
                hour
            }

            val arrivalTotalMinutes = hour24 * 60 + minute
            arrivalTotalMinutes < currentTotalMinutes
        } catch (e: Exception) {
            false
        }
    }

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
        if (uiState.arrivals.isNotEmpty() && uiState.scrollToIndex > 0 && !uiState.isLoading) {
            // Add a small delay to ensure the list is fully rendered
            kotlinx.coroutines.delay(300)
            listState.animateScrollToItem(uiState.scrollToIndex)
        }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetContent = {
            if (uiState.hasSpecialSchedule) {
                SpecialScheduleBottomSheet(
                    onViewSchedule = { viewModel.openSpecialSchedulePdf() }
                )
            }
        },
        sheetPeekHeight = when {
            !uiState.hasSpecialSchedule -> 0.dp
            uiState.hasUserDismissedSheet -> 48.dp
            else -> 120.dp
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            TripConfigurationBar(
                fromStation = uiState.fromStation,
                toStation = uiState.toStation,
                onFromStationChange = { viewModel.updateFromStation(it) },
                onToStationChange = { viewModel.updateToStation(it) },
                onReverseStationsClick = { viewModel.reverseStations() },
                stations = viewModel.stationOptions
            )

            // Schedule list with animated shimmer loading
            Box(modifier = Modifier.fillMaxSize()) {
                // Shimmer loading state
                if (uiState.isLoading || shimmerAlpha > 0f) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(bottom = 0.dp)
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
                            .padding(horizontal = 8.dp)
                            .alpha(contentAlpha),
                        contentPadding = PaddingValues(bottom = 0.dp)
                    ) {
                        itemsIndexed(uiState.arrivals) { index, arrival ->
                            val isPast = isArrivalInPast(arrival)
                            ScheduleItem(
                                arrival = arrival,
                                isHighlighted = index == uiState.scrollToIndex,
                                isPast = isPast,
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
}

@Preview(showBackground = true)
@Composable
fun SchedulesScreenPreview() {
    MaterialTheme {
        // Create a mock UI state for preview
        val mockArrivals = listOf(
            Arrival("8:15 AM", "8:45 AM"),
            Arrival("8:30 AM", "9:00 AM"),
            Arrival("8:45 AM", "9:15 AM"),
            Arrival("9:00 AM", "9:30 AM"),
            Arrival("9:15 AM", "9:45 AM"),
            Arrival("9:30 AM", "10:00 AM"),
            Arrival("9:45 AM", "10:15 AM"),
            Arrival("10:00 AM", "10:30 AM"),
            Arrival("10:15 AM", "10:45 AM"),
            Arrival("10:30 AM", "11:00 AM"),
            Arrival("10:45 AM", "11:15 AM"),
            Arrival("11:00 AM", "11:30 AM"),
            Arrival("11:15 AM", "11:45 AM"),
            Arrival("11:30 AM", "12:00 PM"),
            Arrival("11:45 AM", "12:15 PM"),
            Arrival("12:00 PM", "12:30 PM")
        )

        false.SchedulesScreenContent(
            arrivals = mockArrivals,
            fromStation = "Lindenwold",
            toStation = "15–16th & Locust",
            hasSpecialSchedule = true,
            stationOptions = listOf("Lindenwold", "15–16th & Locust"),
            onFromStationChange = { },
            onToStationChange = { },
            onReverseStationsClick = { },
            onOpenSpecialSchedulePdf = { }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Boolean.SchedulesScreenContent(
    arrivals: List<Arrival>,
    fromStation: String,
    toStation: String,
    hasSpecialSchedule: Boolean,
    stationOptions: List<String>,
    onFromStationChange: (String) -> Unit,
    onToStationChange: (String) -> Unit,
    onReverseStationsClick: () -> Unit,
    onOpenSpecialSchedulePdf: () -> Unit
) {
    val listState = rememberLazyListState()

    // Bottom sheet state for preview
    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = if (hasSpecialSchedule && !this) SheetValue.PartiallyExpanded else SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetContent = {
            if (hasSpecialSchedule) {
                SpecialScheduleBottomSheet(
                    onViewSchedule = onOpenSpecialSchedulePdf
                )
            }
        },
        sheetPeekHeight = when {
            !hasSpecialSchedule -> 0.dp
            this -> 48.dp
            else -> 120.dp
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            TripConfigurationBar(
                fromStation = fromStation,
                toStation = toStation,
                onFromStationChange = onFromStationChange,
                onToStationChange = onToStationChange,
                onReverseStationsClick = onReverseStationsClick,
                stations = stationOptions
            )

            // Schedule list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp),
                contentPadding = PaddingValues(bottom = 0.dp)
            ) {
                itemsIndexed(arrivals) { index, arrival ->
                    ScheduleItem(
                        arrival = arrival,
                        isHighlighted = index == 0,
                        isPast = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (index < arrivals.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
