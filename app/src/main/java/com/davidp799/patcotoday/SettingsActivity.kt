package com.davidp799.patcotoday

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import kotlin.random.Random

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity_scrolling)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_fragment_container, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // User Interface
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Shared Preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "device_theme" -> {
                val pref = sharedPreferences?.getString(key, "3")
                when (pref?.toInt()) {
                    1 -> AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                    )
                    2 -> AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                    )
                    3 -> AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
                }
            }
            "dynamic_colors" -> {
                val pref = sharedPreferences?.getBoolean(key, false)
                if (pref == true) {
                    DynamicColors.applyToActivitiesIfAvailable(application)
                }
            }
            "download_on_mobile_data" -> {
            }
        }
        recreate()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private var easterEggCounter = 0
        private val easterEggs = listOf(
            "[“hip”,”hip”] (hip hip array!)",
            "//be nice to the CPU. Thread_sleep(1);",
            "!false. It's funny because it's true.",
            "If you listen to a UNIX shell, can you hear the C?",
            "An SQL query goes into a bar, walks up to two tables and asks: 'Can I join you?'",
            "I went to a street where the houses were numbered 8k, 16k, 32k, 64k, 128k, 256k and 512k. It was a trip down Memory Lane."
        )

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val dynamicColorsPref = findPreference<SwitchPreferenceCompat>("dynamic_colors")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                dynamicColorsPref?.isVisible = false
            }
        } // set as xml.preferences

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            when (preference.key) {
                "app_version" -> {
                    easterEggCounter += 1
                    if(easterEggCounter == 5) {
                        val easterEggSelection = Random.nextInt(0, easterEggs.size)
                        Toast.makeText(
                            context,
                            easterEggs[easterEggSelection],
                            Toast.LENGTH_LONG
                        ).show()
                        easterEggCounter = 0
                    }
                }
                "check_updates" -> {
                    // TODO: check for updates here
                    Toast.makeText(
                        context,
                        "Your schedules are up to date.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else -> {}
            }
            return super.onPreferenceTreeClick(preference)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}