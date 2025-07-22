package com.davidp799.patcotoday.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidp799.patcotoday.ui.screens.models.Station

@Composable
fun StationListItem(
    station: Station,
    position: Int,
    totalStations: Int,
    onStationClick: (Station) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStationClick(station) }
            .padding(horizontal = 16.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Timeline icon representation
        Box(
            modifier = Modifier.width(60.dp),
            contentAlignment = Alignment.Center
        ) {
            TimelineIcon(
                position = position,
                totalStations = totalStations
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Center: Station name
        Text(
            text = station.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // Right side: Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Station details",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimelineIcon(
    position: Int,
    totalStations: Int
) {
    val isFirst = position == 0
    val isLast = position == totalStations - 1

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        // Top line (hidden for first station)
        if (!isFirst) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Divider(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 3.dp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Station dot
        Box(
            modifier = Modifier
                .size(if (isFirst || isLast) 16.dp else 12.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = "Station",
                tint = when {
                    isFirst -> MaterialTheme.colorScheme.secondary
                    isLast -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom line (hidden for last station)
        if (!isLast) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Divider(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 3.dp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
