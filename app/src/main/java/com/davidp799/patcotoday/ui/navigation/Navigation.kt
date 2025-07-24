package com.davidp799.patcotoday.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.davidp799.patcotoday.ui.screens.InformationScreen
import com.davidp799.patcotoday.ui.screens.SchedulesScreen
import com.davidp799.patcotoday.ui.screens.SchedulesScreenViewModel
import com.davidp799.patcotoday.ui.screens.StationMapScreen

// MaterialFadeThrough transition constants
private const val FADE_THROUGH_DURATION = 200
private const val FADE_THROUGH_DELAY = 90
private const val SCALE_FACTOR = 0.96f

@Composable
fun Navigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    schedulesViewModel: SchedulesScreenViewModel? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Schedules.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Schedules.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = FADE_THROUGH_DURATION,
                        delayMillis = FADE_THROUGH_DELAY
                    )
                ) + scaleIn(
                    initialScale = SCALE_FACTOR,
                    animationSpec = tween(
                        durationMillis = FADE_THROUGH_DURATION,
                        delayMillis = FADE_THROUGH_DELAY
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(durationMillis = FADE_THROUGH_DURATION)
                ) + scaleOut(
                    targetScale = SCALE_FACTOR,
                    animationSpec = tween(durationMillis = FADE_THROUGH_DURATION)
                )
            }
        ) {
            if (schedulesViewModel != null) {
                SchedulesScreen(viewModel = schedulesViewModel)
            } else {
                SchedulesScreen()
            }
        }
        composable(
            route = Screen.StationMap.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = FADE_THROUGH_DURATION,
                        delayMillis = FADE_THROUGH_DELAY
                    )
                ) + scaleIn(
                    initialScale = SCALE_FACTOR,
                    animationSpec = tween(
                        durationMillis = FADE_THROUGH_DURATION,
                        delayMillis = FADE_THROUGH_DELAY
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(durationMillis = FADE_THROUGH_DURATION)
                ) + scaleOut(
                    targetScale = SCALE_FACTOR,
                    animationSpec = tween(durationMillis = FADE_THROUGH_DURATION)
                )
            }
        ) {
            StationMapScreen()
        }
        composable(
            route = Screen.Information.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = FADE_THROUGH_DURATION,
                        delayMillis = FADE_THROUGH_DELAY
                    )
                ) + scaleIn(
                    initialScale = SCALE_FACTOR,
                    animationSpec = tween(
                        durationMillis = FADE_THROUGH_DURATION,
                        delayMillis = FADE_THROUGH_DELAY
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(durationMillis = FADE_THROUGH_DURATION)
                ) + scaleOut(
                    targetScale = SCALE_FACTOR,
                    animationSpec = tween(durationMillis = FADE_THROUGH_DURATION)
                )
            }
        ) {
            InformationScreen()
        }
    }
}
