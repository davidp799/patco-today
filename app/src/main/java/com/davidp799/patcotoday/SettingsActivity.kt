package com.davidp799.patcotoday

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import com.davidp799.patcotoday.ui.screens.SchedulesScreenViewModel
import com.davidp799.patcotoday.ui.theme.PatcoTodayTheme
import kotlin.random.Random

class SettingsActivity : ComponentActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get theme preferences to configure system bars
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themePreference = prefs.getString("device_theme", "3")?.toInt() ?: 3
        val isSystemInDarkTheme = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        // Determine if we should use dark theme
        val useDarkTheme = when (themePreference) {
            1 -> false // Light theme
            2 -> true  // Dark theme
            3 -> isSystemInDarkTheme // Follow system
            else -> isSystemInDarkTheme
        }

        // Configure edge-to-edge with proper system bar styles
        enableEdgeToEdge(
            statusBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            },
            navigationBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            }
        )

        // Register preference change listener
        prefs.registerOnSharedPreferenceChangeListener(this)

        setContent {
            PatcoTodayTheme {
                SettingsScreenWithBlur()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "device_theme" -> {
                val pref = sharedPreferences?.getString(key, "3")
                when (pref?.toInt()) {
                    1 -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                    )
                    2 -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                    )
                    3 -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
                }
            }
            "dynamic_colors" -> {
                val pref = sharedPreferences?.getBoolean(key, false)
                if (pref == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    com.google.android.material.color.DynamicColors.applyToActivitiesIfAvailable(application)
                }
            }
        }
        recreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}

@Composable
fun SettingsScreenWithBlur() {
    // Create ViewModel instance for settings activity
    val schedulesViewModel: SchedulesScreenViewModel = viewModel()
    val schedulesUiState by schedulesViewModel.uiState.collectAsState()

    // Animate overlay alpha when refreshing schedules
    val overlayAlpha by animateFloatAsState(
        targetValue = if (schedulesUiState.isRefreshing) 0.3f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "settings_overlay_fade"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreen(schedulesViewModel = schedulesViewModel)

        // Blur overlay when refreshing schedules
        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = overlayAlpha)
                    )
            )
        }

        // Loading indicator on top of blur
        if (schedulesUiState.isRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(schedulesViewModel: SchedulesScreenViewModel? = null) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val sharedPrefs = remember {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    // Set up toast callback for the ViewModel when available
    LaunchedEffect(schedulesViewModel) {
        schedulesViewModel?.setShowToastCallback { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Get refresh state for blur effect
    val schedulesUiState by (schedulesViewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(com.davidp799.patcotoday.ui.screens.SchedulesUiState())
    })

    // Animate blur effect
    val blurRadius by animateFloatAsState(
        targetValue = if (schedulesUiState.isRefreshing) 8f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "settings_content_blur"
    )

    // State for preferences
    var selectedTheme by remember { mutableIntStateOf(sharedPrefs.getString("device_theme", "3")?.toInt() ?: 3) }
    var dynamicColors by remember { mutableStateOf(sharedPrefs.getBoolean("dynamic_colors", false)) }
    var downloadOnMobileData by remember { mutableStateOf(sharedPrefs.getBoolean("download_on_mobile_data", true)) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var easterEggCounter by remember { mutableIntStateOf(0) }

    // Easter eggs from legacy app
    val easterEggs = listOf(
        "[\"hip\",\"hip\"] (hip hip array!)",
        "//be nice to the CPU. Thread_sleep(1);",
        "!false. It's funny because it's true.",
        "If you listen to a UNIX shell, can you hear the C?",
        "An SQL query goes into a bar, walks up to two tables and asks: 'Can I join you?'",
        "I went to a street where the houses were numbered 8k, 16k, 32k, 64k, 128k, 256k and 512k. It was a trip down Memory Lane."
    )

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .blur(radius = blurRadius.dp),
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Display Category
            item {
                SettingsCategoryHeader("Display")
            }

            // Theme Preference
            item {
                SettingsItem(
                    title = stringResource(R.string.pref_theme),
                    summary = when (selectedTheme) {
                        1 -> "Light"
                        2 -> "Dark"
                        3 -> "Follow system"
                        else -> "Follow system"
                    },
                    onClick = { showThemeDialog = true }
                )
            }

            // Dynamic Colors (only show on Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    SettingsSwitchItem(
                        title = stringResource(R.string.pref_dynamic),
                        summary = stringResource(R.string.pref_dynamic_summary),
                        checked = dynamicColors,
                        onCheckedChange = { checked ->
                            dynamicColors = checked
                            sharedPrefs.edit().putBoolean("dynamic_colors", checked).apply()
                        }
                    )
                }
            }

            // More Category
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsCategoryHeader("More")
            }

            // Check Updates
            item {
                SettingsItem(
                    title = stringResource(R.string.pref_title_updates),
                    summary = stringResource(R.string.pref_summary_updates),
                    onClick = {
                        schedulesViewModel?.refreshSchedules()
                    }
                )
            }

            // Download on Mobile Data
            item {
                SettingsSwitchItem(
                    title = stringResource(R.string.download_on_mobile_data),
                    summary = stringResource(R.string.download_on_mobile_data_summary),
                    checked = downloadOnMobileData,
                    onCheckedChange = { checked ->
                        downloadOnMobileData = checked
                        sharedPrefs.edit().putBoolean("download_on_mobile_data", checked).apply()
                    }
                )
            }

            // Feedback
            item {
                SettingsItem(
                    title = stringResource(R.string.pref_title_feedback),
                    summary = stringResource(R.string.pref_summary_feedback),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("mailto:david.r.pape@gmail.com")
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // About Category
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsCategoryHeader("About")
            }

            // App Version (with easter egg)
            item {
                SettingsItem(
                    title = stringResource(R.string.pref_title_version),
                    summary = BuildConfig.VERSION_NAME,
                    onClick = {
                        easterEggCounter++
                        if (easterEggCounter == 5) {
                            val easterEggSelection = Random.nextInt(0, easterEggs.size)
                            Toast.makeText(context, easterEggs[easterEggSelection], Toast.LENGTH_LONG).show()
                            easterEggCounter = 0
                        }
                    }
                )
            }

            // Privacy Policy
            item {
                SettingsItem(
                    title = stringResource(R.string.privacy_title),
                    summary = stringResource(R.string.privacy_summary),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://www.termsfeed.com/live/7267d0fc-3b09-435a-bf45-71ead0cc1494")
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // Terms & Conditions
            item {
                SettingsItem(
                    title = stringResource(R.string.terms_title),
                    summary = stringResource(R.string.terms_summary),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://www.termsfeed.com/live/e56e7ea1-704d-45cf-9b6c-36c786290c1b")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            selectedTheme = selectedTheme,
            onThemeSelected = { theme ->
                selectedTheme = theme
                sharedPrefs.edit().putString("device_theme", theme.toString()).apply()
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    summary: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (summary != null) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    summary: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (summary != null) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    selectedTheme: Int,
    onThemeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val themeOptions = listOf(
        1 to "Light",
        2 to "Dark",
        3 to "Follow system"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose theme") },
        text = {
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                themeOptions.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedTheme == value,
                                onClick = { onThemeSelected(value) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTheme == value,
                            onClick = null
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
