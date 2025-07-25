package com.davidp799.patcotoday.ui.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    // Detect dark mode using background luminance
    val backgroundColor = MaterialTheme.colorScheme.background
    val isDarkMode = (backgroundColor.red + backgroundColor.green + backgroundColor.blue) / 3f < 0.5f
    val overlayAlpha = if (isDarkMode) 0.80f else 0.70f

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated background (same as WhatsNewScreen)
        AnimatedBackground()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background.copy(alpha = overlayAlpha)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Patco Today! 🚆",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("🕒 View accurate schedules anytime, anywhere.")
                        }
                        append("\nNever miss your train with real-time schedule information at your fingertips.\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("⚠️ See special schedules as soon as they appear.")
                        }
                        append("\nStay informed about service changes, holiday schedules, and special events.\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("ℹ️ View information about every Patco Station.")
                        }
                        append("\nExplore comprehensive details about all stations along the line.\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("🔍 Find anything and everything Patco.")
                        }
                        append("\nYour complete guide to the PATCO Hi-Speedline system.\n\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("⚙️ Make Patco Today yours.")
                        }
                        append("\nCustomize your experience with theme settings, schedule preferences, and more.")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onGetStarted) {
                    Text("Get Started! 🚀")
                }
            }
        }
    }
}

@Composable
private fun AnimatedBackground() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    // More reliable dark mode detection using background luminance
    val backgroundColor = MaterialTheme.colorScheme.background
    val isDarkMode = (backgroundColor.red + backgroundColor.green + backgroundColor.blue) / 3f < 0.5f

    // Animation states with varied, fairy-like timing
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")

    // Multiple independent movement patterns for fairy-like motion
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = FastOutSlowInEasing), // Slower, more organic
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation1"
    )

    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation2"
    )

    val rotation3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation3"
    )

    // Fairy-like floating movements
    val floatX1 by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatX1"
    )

    val floatY1 by infiniteTransition.animateFloat(
        initialValue = -150f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY1"
    )

    val floatX2 by infiniteTransition.animateFloat(
        initialValue = -180f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatX2"
    )

    val floatY2 by infiniteTransition.animateFloat(
        initialValue = -120f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY2"
    )

    val floatX3 by infiniteTransition.animateFloat(
        initialValue = -160f,
        targetValue = 160f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatX3"
    )

    val floatY3 by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY3"
    )

    // Gentle pulsing like fairy lights
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse3"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawFairyLikeBackground(
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            tertiaryColor = tertiaryColor,
            isDarkMode = isDarkMode,
            rotation1 = rotation1,
            rotation2 = rotation2,
            rotation3 = rotation3,
            floatX1 = floatX1,
            floatY1 = floatY1,
            floatX2 = floatX2,
            floatY2 = floatY2,
            floatX3 = floatX3,
            floatY3 = floatY3,
            pulse1 = pulse1,
            pulse2 = pulse2,
            pulse3 = pulse3
        )
    }
}

private fun DrawScope.drawFairyLikeBackground(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    isDarkMode: Boolean,
    rotation1: Float,
    rotation2: Float,
    rotation3: Float,
    floatX1: Float,
    floatY1: Float,
    floatX2: Float,
    floatY2: Float,
    floatX3: Float,
    floatY3: Float,
    pulse1: Float,
    pulse2: Float,
    pulse3: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val maxRadius = size.width.coerceAtLeast(size.height) * 0.4f

    // Adjust alpha multiplier based on theme - more visible fairies in both modes
    val alphaMultiplier = if (isDarkMode) 2.5f else 2.0f

    // Fairy 1 - Primary color, top-left quadrant wandering
    val fairy1X = size.width * 0.25f + floatX1 + cos(Math.toRadians(rotation1.toDouble())).toFloat() * 50f
    val fairy1Y = size.height * 0.3f + floatY1 + sin(Math.toRadians(rotation1.toDouble())).toFloat() * 30f
    val fairy1Radius = maxRadius * 0.6f * pulse1

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.35f * pulse1 * alphaMultiplier),
                primaryColor.copy(alpha = 0.30f * pulse1 * alphaMultiplier),
                primaryColor.copy(alpha = 0.25f * pulse1 * alphaMultiplier),
                primaryColor.copy(alpha = 0.18f * pulse1 * alphaMultiplier),
                primaryColor.copy(alpha = 0.10f * pulse1 * alphaMultiplier),
                Color.Transparent
            ),
            center = Offset(fairy1X, fairy1Y),
            radius = fairy1Radius
        ),
        radius = fairy1Radius,
        center = Offset(fairy1X, fairy1Y)
    )

    // Fairy 2 - Secondary color, top-right quadrant wandering
    val fairy2X = size.width * 0.75f + floatX2 + cos(Math.toRadians(rotation2.toDouble() + 45)).toFloat() * 60f
    val fairy2Y = size.height * 0.25f + floatY2 + sin(Math.toRadians(rotation2.toDouble() + 45)).toFloat() * 40f
    val fairy2Radius = maxRadius * 0.5f * pulse2

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                secondaryColor.copy(alpha = 0.38f * pulse2 * alphaMultiplier),
                secondaryColor.copy(alpha = 0.32f * pulse2 * alphaMultiplier),
                secondaryColor.copy(alpha = 0.27f * pulse2 * alphaMultiplier),
                secondaryColor.copy(alpha = 0.20f * pulse2 * alphaMultiplier),
                secondaryColor.copy(alpha = 0.12f * pulse2 * alphaMultiplier),
                Color.Transparent
            ),
            center = Offset(fairy2X, fairy2Y),
            radius = fairy2Radius
        ),
        radius = fairy2Radius,
        center = Offset(fairy2X, fairy2Y)
    )

    // Fairy 3 - Tertiary color, bottom area wandering
    val fairy3X = size.width * 0.4f + floatX3 + cos(Math.toRadians(rotation3.toDouble() + 90)).toFloat() * 70f
    val fairy3Y = size.height * 0.7f + floatY3 + sin(Math.toRadians(rotation3.toDouble() + 90)).toFloat() * 35f
    val fairy3Radius = maxRadius * 0.7f * pulse3

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                tertiaryColor.copy(alpha = 0.33f * pulse3 * alphaMultiplier),
                tertiaryColor.copy(alpha = 0.28f * pulse3 * alphaMultiplier),
                tertiaryColor.copy(alpha = 0.24f * pulse3 * alphaMultiplier),
                tertiaryColor.copy(alpha = 0.17f * pulse3 * alphaMultiplier),
                tertiaryColor.copy(alpha = 0.09f * pulse3 * alphaMultiplier),
                Color.Transparent
            ),
            center = Offset(fairy3X, fairy3Y),
            radius = fairy3Radius
        ),
        radius = fairy3Radius,
        center = Offset(fairy3X, fairy3Y)
    )

    // Fairy 4 - Small primary, right side
    val fairy4X = size.width * 0.85f + floatX1 * 0.5f + cos(Math.toRadians(-rotation1.toDouble() + 120)).toFloat() * 40f
    val fairy4Y = size.height * 0.6f + floatY1 * 0.7f + sin(Math.toRadians(-rotation1.toDouble() + 120)).toFloat() * 25f
    val fairy4Radius = maxRadius * 0.3f * pulse1 * 0.8f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.28f * pulse1 * alphaMultiplier),
                primaryColor.copy(alpha = 0.23f * pulse1 * alphaMultiplier),
                primaryColor.copy(alpha = 0.18f * pulse1 * alphaMultiplier),
                primaryColor.copy(alpha = 0.11f * pulse1 * alphaMultiplier),
                Color.Transparent
            ),
            center = Offset(fairy4X, fairy4Y),
            radius = fairy4Radius
        ),
        radius = fairy4Radius,
        center = Offset(fairy4X, fairy4Y)
    )

    // Fairy 5 - Small secondary, left side
    val fairy5X = size.width * 0.15f + floatX2 * 0.6f + cos(Math.toRadians(rotation2.toDouble() + 200)).toFloat() * 35f
    val fairy5Y = size.height * 0.8f + floatY2 * 0.5f + sin(Math.toRadians(rotation2.toDouble() + 200)).toFloat() * 30f
    val fairy5Radius = maxRadius * 0.35f * pulse2 * 0.9f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                secondaryColor.copy(alpha = 0.30f * pulse2 * alphaMultiplier),
                secondaryColor.copy(alpha = 0.25f * pulse2 * alphaMultiplier),
                secondaryColor.copy(alpha = 0.20f * pulse2 * alphaMultiplier),
                secondaryColor.copy(alpha = 0.13f * pulse2 * alphaMultiplier),
                Color.Transparent
            ),
            center = Offset(fairy5X, fairy5Y),
            radius = fairy5Radius
        ),
        radius = fairy5Radius,
        center = Offset(fairy5X, fairy5Y)
    )

    // Fairy 6 - Tiny tertiary, center-top
    val fairy6X = size.width * 0.6f + floatX3 * 0.4f + cos(Math.toRadians(rotation3.toDouble() + 300)).toFloat() * 20f
    val fairy6Y = size.height * 0.15f + floatY3 * 0.3f + sin(Math.toRadians(rotation3.toDouble() + 300)).toFloat() * 15f
    val fairy6Radius = maxRadius * 0.25f * pulse3 * 0.7f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                tertiaryColor.copy(alpha = 0.25f * pulse3 * alphaMultiplier),
                tertiaryColor.copy(alpha = 0.21f * pulse3 * alphaMultiplier),
                tertiaryColor.copy(alpha = 0.16f * pulse3 * alphaMultiplier),
                tertiaryColor.copy(alpha = 0.10f * pulse3 * alphaMultiplier),
                Color.Transparent
            ),
            center = Offset(fairy6X, fairy6Y),
            radius = fairy6Radius
        ),
        radius = fairy6Radius,
        center = Offset(fairy6X, fairy6Y)
    )

    // Subtle ambient glow across the screen - stronger in dark mode
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.01f * alphaMultiplier),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = size.maxDimension
        )
    )
}
