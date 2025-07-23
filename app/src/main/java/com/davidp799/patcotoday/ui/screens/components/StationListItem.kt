package com.davidp799.patcotoday.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidp799.patcotoday.ui.screens.models.Station
import androidx.compose.ui.tooling.preview.Preview
import com.davidp799.patcotoday.ui.screens.models.StationAmenities
import com.davidp799.patcotoday.ui.screens.models.FareZone
import com.davidp799.patcotoday.ui.screens.models.FareInfo

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
                    .clip(RoundedCornerShape(bottomStart = 1.5.dp, bottomEnd = 1.5.dp)),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 300.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Station dot
        Box(
            modifier = Modifier
                .size(24.dp)
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
                    .clip(RoundedCornerShape(topStart = 1.5.dp, topEnd = 1.5.dp)),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 300.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StationListItemPreview() {
    MaterialTheme {
        // Mock station data for preview
        val mockStation = Station(
            id = "lindenwold",
            name = "Lindenwold",
            fullName = "Lindenwold Station",
            address = "901 N. Berlin Road, Lindenwold, NJ 08021",
            amenities = StationAmenities(
                elevator = "Elevator",
                escalator = "Escalator",
                bikeRacks = "Bike Racks",
                taxiService = "Taxi Service",
                parking = "Parking"
            ),
            hours = "Station is open 24/7.",
            gatedParking = "$1 from 5 am to 10 am (pay with FREEDOM card only). Free after 10 am.",
            meters = "",
            walkingDistance = "UMDNJ-School of Osteopathic Medicine\nKennedy University Hospital",
            fareZone = FareZone.LINDENWOLD_GROUP,
            fareInfo = FareInfo("$1.60", "$3.20", "$3.00", "$6.00")
        )

        Column {
            // Show different positions in the timeline
            StationListItem(
                station = mockStation.copy(name = "Lindenwold"),
                position = 0,
                totalStations = 14,
                onStationClick = { }
            )
            HorizontalDivider()
            StationListItem(
                station = mockStation.copy(name = "Haddonfield"),
                position = 3,
                totalStations = 14,
                onStationClick = { }
            )
            HorizontalDivider()
            StationListItem(
                station = mockStation.copy(name = "15–16th & Locust"),
                position = 13,
                totalStations = 14,
                onStationClick = { }
            )
        }
    }
}
