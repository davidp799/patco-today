package com.davidp799.patcotoday.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.twotone.Circle
import androidx.compose.material.icons.twotone.MoreVert
import androidx.compose.material.icons.twotone.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripConfigurationBar(
    fromStation: String,
    toStation: String,
    onFromStationChange: (String) -> Unit,
    onToStationChange: (String) -> Unit,
    onReverseStationsClick: () -> Unit,
    stations: List<String> = defaultStations
) {
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    // Track rotation state for the reverse button animation
    var isReversed by remember { mutableStateOf(false) }

    // Animate rotation based on isReversed state
    val rotationAngle by animateFloatAsState(
        targetValue = if (isReversed) 180f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "reverse_button_rotation"
    )

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(0.15f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.TwoTone.Circle,
                    contentDescription = "Source Icon",
                    modifier = Modifier.padding(start = 10.dp)
                )
                Icon(
                    imageVector = Icons.TwoTone.MoreVert,
                    contentDescription = "Dots Icon",
                    modifier = Modifier.padding(start = 10.dp)
                )
                Icon(
                    imageVector = Icons.TwoTone.Place,
                    contentDescription = "Destination Icon",
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            // Conditional layout based on orientation
            if (isLandscape) {
                // Landscape: Side by side layout
                Row(
                    modifier = Modifier
                        .weight(0.70f)
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // From Station Dropdown
                    ExposedDropdownMenuBox(
                        expanded = fromExpanded,
                        onExpandedChange = { fromExpanded = !fromExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = fromStation,
                            onValueChange = onFromStationChange,
                            label = { Text("From") },
                            shape = RoundedCornerShape(12.dp),
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = fromExpanded
                                )
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = fromExpanded,
                            onDismissRequest = { fromExpanded = false },
                            modifier = Modifier.exposedDropdownSize()
                        ) {
                            stations.forEach { station ->
                                DropdownMenuItem(
                                    text = { Text(station) },
                                    onClick = {
                                        onFromStationChange(station)
                                        fromExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // To Station Dropdown
                    ExposedDropdownMenuBox(
                        expanded = toExpanded,
                        onExpandedChange = { toExpanded = !toExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = toStation,
                            onValueChange = onToStationChange,
                            label = { Text("To") },
                            shape = RoundedCornerShape(12.dp),
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = toExpanded
                                )
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = toExpanded,
                            onDismissRequest = { toExpanded = false },
                            modifier = Modifier.exposedDropdownSize()
                        ) {
                            stations.forEach { station ->
                                DropdownMenuItem(
                                    text = { Text(station) },
                                    onClick = {
                                        onToStationChange(station)
                                        toExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Portrait: Vertical layout (original)
                Column(
                    modifier = Modifier
                        .weight(0.70f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // From Station Dropdown
                    ExposedDropdownMenuBox(
                        expanded = fromExpanded,
                        onExpandedChange = { fromExpanded = !fromExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        OutlinedTextField(
                            value = fromStation,
                            onValueChange = onFromStationChange,
                            label = { Text("From") },
                            shape = RoundedCornerShape(12.dp),
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = fromExpanded
                                )
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = fromExpanded,
                            onDismissRequest = { fromExpanded = false },
                            modifier = Modifier.exposedDropdownSize()
                        ) {
                            stations.forEach { station ->
                                DropdownMenuItem(
                                    text = { Text(station) },
                                    onClick = {
                                        onFromStationChange(station)
                                        fromExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // To Station Dropdown
                    ExposedDropdownMenuBox(
                        expanded = toExpanded,
                        onExpandedChange = { toExpanded = !toExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        OutlinedTextField(
                            value = toStation,
                            onValueChange = onToStationChange,
                            label = { Text("To") },
                            shape = RoundedCornerShape(12.dp),
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = toExpanded
                                )
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = toExpanded,
                            onDismissRequest = { toExpanded = false },
                            modifier = Modifier.exposedDropdownSize()
                        ) {
                            stations.forEach { station ->
                                DropdownMenuItem(
                                    text = { Text(station) },
                                    onClick = {
                                        onToStationChange(station)
                                        toExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(0.15f),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        // Toggle rotation state and call the original callback
                        isReversed = !isReversed
                        onReverseStationsClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwapVert,
                        contentDescription = "Reverse Stations",
                        modifier = Modifier
                            .size(48.dp)
                            .rotate(rotationAngle)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TripConfigurationBarPreview() {
    TripConfigurationBar(
        fromStation = "Lindenwold",
        toStation = "15-16th & Locust",
        onFromStationChange = { },
        onToStationChange = { },
        onReverseStationsClick = { }
    )
}

// Default list of PATCO stations
private val defaultStations = listOf(
    "Lindenwold",
    "Ashland",
    "Woodcrest",
    "Haddonfield",
    "Westmont",
    "Collingswood",
    "Ferry Avenue",
    "Broadway",
    "City Hall",
    "Franklin Square",
    "8th & Market",
    "9–10th & Locust",
    "12–13th & Locust",
    "15–16th & Locust"
)
