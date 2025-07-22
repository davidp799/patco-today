package com.davidp799.patcotoday.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.*
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InformationScreen() {
    val context = LocalContext.current

    // Data arrays matching the original ViewModel
    val infoItems = arrayOf(
        "Fares", "Reload Freedom Card", "Twitter", "Call", "Email", "Website", "Special Schedules",
        "Elevator & Escalator Availability", "FAQ's", "Safety & Security"
    )

    val infoLinks = arrayOf(
        "https://www.ridepatco.org/schedules/fares.html",
        "https://www.patcofreedomcard.org/front/account/login.jsp",
        "https://twitter.com/RidePATCO",
        "tel:+1-856-772-6900",
        "mailto:patco@ridepatco.org",
        "https://www.ridepatco.org/index.asp",
        "https://www.ridepatco.org/schedules/schedules.asp",
        "https://www.ridepatco.org/schedules/alerts_more.asp?page=25",
        "https://www.ridepatco.org/travel/faqs.html",
        "https://www.ridepatco.org/safety/how_do_i.html"
    )

    // Icon mapping using Material Icons TwoTone
    val infoIcons = arrayOf(
        Icons.TwoTone.AttachMoney,              // Fares
        Icons.TwoTone.CreditCard,               // Reload Freedom Card
        Icons.AutoMirrored.TwoTone.Message,     // Twitter
        Icons.TwoTone.Phone,                    // Call
        Icons.TwoTone.Email,                    // Email
        Icons.TwoTone.Language,                 // Website
        Icons.TwoTone.Schedule,                 // Special Schedules
        Icons.AutoMirrored.TwoTone.Accessible,  // Elevator & Escalator Availability
        Icons.AutoMirrored.TwoTone.Help,        // FAQ's
        Icons.TwoTone.Security                  // Safety & Security
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(0.dp)
    ) {
        itemsIndexed(infoItems) { index, item ->
            InformationItem(
                icon = infoIcons[index],
                text = item,
                onClick = {
                    try {
                        if (item == "Fares") {
                            // TODO: Navigate to Fares detail screen
                            // For now, open the link
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(infoLinks[index]))
                            context.startActivity(intent)
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(infoLinks[index]))
                            context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )

            // Add divider except for the last item
            if (index < infoItems.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun InformationItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Information icon",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(48.dp))

        Text(
            text = text,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
    }
}
