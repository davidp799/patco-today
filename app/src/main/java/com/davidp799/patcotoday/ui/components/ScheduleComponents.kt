package com.davidp799.patcotoday.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowRightAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidp799.patcotoday.utils.Arrival

@Composable
fun DropShadow(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun ScheduleItem(
    arrival: Arrival,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    val textStyle = if (isHighlighted) {
        MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
        )
    } else {
        MaterialTheme.typography.titleLarge
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Arrival time
        Text(
            text = arrival.arrivalTime,
            style = textStyle,
            fontSize = 22.sp,
            modifier = Modifier.weight(0.45f),
            textAlign = TextAlign.Center
        )

        // Arrow
        Box(
            modifier = Modifier.weight(0.10f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowRightAlt,
                contentDescription = "Arrival time arrow",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Travel time
        Text(
            text = arrival.travelTime,
            style = textStyle,
            fontSize = 22.sp,
            modifier = Modifier.weight(0.45f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DropShadowPreview() {
    MaterialTheme {
        DropShadow()
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleItemPreview() {
    MaterialTheme {
        ScheduleItem(
            arrival = Arrival("7:30 AM", "45 min")
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleItemHighlightedPreview() {
    MaterialTheme {
        ScheduleItem(
            arrival = Arrival("8:15 AM", "42 min"),
            isHighlighted = true
        )
    }
}
