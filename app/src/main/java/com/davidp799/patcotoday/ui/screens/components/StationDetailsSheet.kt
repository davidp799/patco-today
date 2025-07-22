package com.davidp799.patcotoday.ui.screens.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidp799.patcotoday.ui.screens.models.Station
import com.davidp799.patcotoday.ui.screens.models.StationAmenities

@Composable
fun StationDetailsSheet(
    station: Station,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.95f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Address (clickable)
            Text(
                text = station.address,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable {
                    openAddress(context, station.address)
                },
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Tap address to open in maps or copy to clipboard",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Station Amenities
            StationAmenitiesSection(station.amenities)

            Spacer(modifier = Modifier.height(24.dp))

            // Station Hours
            if (station.hours.isNotEmpty()) {
                StationInfoSection(
                    title = "Station Hours",
                    content = station.hours
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Fares
            FareSection(station)

            Spacer(modifier = Modifier.height(24.dp))

            // Gated Parking
            if (station.gatedParking.isNotEmpty()) {
                StationInfoSection(
                    title = "Gated Parking",
                    content = station.gatedParking
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Meters
            if (station.meters.isNotEmpty()) {
                StationInfoSection(
                    title = "Meters",
                    content = station.meters
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Walking Distance
            if (station.walkingDistance.isNotEmpty()) {
                StationInfoSection(
                    title = "Within Walking Distance",
                    content = station.walkingDistance
                )
            }
        }
    }
}

@Composable
private fun StationAmenitiesSection(amenities: StationAmenities) {
    Text(
        text = "Station Amenities",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (amenities.hasElevator()) {
            AmenityItem(Icons.Default.Elevator, amenities.elevator!!)
        }
        if (amenities.hasEscalator()) {
            AmenityItem(Icons.Default.Stairs, amenities.escalator!!)
        }
        if (amenities.hasBikeRacks()) {
            AmenityItem(Icons.AutoMirrored.Filled.DirectionsBike, amenities.bikeRacks!!)
        }
        if (amenities.hasTaxiService()) {
            AmenityItem(Icons.Default.LocalTaxi, amenities.taxiService!!)
        }
        if (amenities.hasParking()) {
            AmenityItem(Icons.Default.LocalParking, amenities.parking!!)
        }
    }
}

@Composable
private fun AmenityItem(icon: ImageVector, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun FareSection(station: Station) {
    Text(
        text = "Fares",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    FareTable(station)

    Spacer(modifier = Modifier.height(16.dp))

    // Child Fares
    Text(
        text = "Child Fares",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Children under 5 ride free. Children 5-11 pay half fare.",
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Reduced Fares
    Text(
        text = "Reduced Fares",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Senior Citizens (65+) and persons with disabilities pay half fare with proper ID.",
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Fare Options
    Text(
        text = "Fare Options",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Purchase tickets at station vending machines or use the FREEDOM card for convenient travel.",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun FareTable(station: Station) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header Row
            FareTableRow(
                from = "From",
                to = "To",
                oneWay = "One Way",
                roundTrip = "Round Trip",
                isHeader = true
            )

            HorizontalDivider()

            // Station to Philadelphia
            FareTableRow(
                from = station.name,
                to = "Philadelphia",
                oneWay = station.fareInfo.philadelphiaOneWay,
                roundTrip = station.fareInfo.philadelphiaRoundTrip,
                isHeader = false
            )

            HorizontalDivider()

            // Station to Any NJ
            FareTableRow(
                from = station.name,
                to = "Any NJ",
                oneWay = station.fareInfo.newJerseyOneWay,
                roundTrip = station.fareInfo.newJerseyRoundTrip,
                isHeader = false
            )

            // Special case for Broadway Station
            if (station.name == "Broadway") {
                HorizontalDivider()
                FareTableRow(
                    from = "Broadway",
                    to = "City Hall",
                    oneWay = "$1.40",
                    roundTrip = "$2.80",
                    isHeader = false
                )
            }
        }
    }
}

@Composable
private fun FareTableRow(
    from: String,
    to: String,
    oneWay: String,
    roundTrip: String,
    isHeader: Boolean
) {
    val backgroundColor = if (isHeader) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = from,
            modifier = Modifier.weight(0.25f),
            style = if (isHeader) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                   else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = to,
            modifier = Modifier.weight(0.25f),
            style = if (isHeader) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                   else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = oneWay,
            modifier = Modifier.weight(0.25f),
            style = if (isHeader) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                   else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = roundTrip,
            modifier = Modifier.weight(0.25f),
            style = if (isHeader) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                   else MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun StationInfoSection(title: String, content: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium
    )
}

private fun openAddress(context: Context, address: String) {
    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        // Fallback: copy address to clipboard
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Station Address", address)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
