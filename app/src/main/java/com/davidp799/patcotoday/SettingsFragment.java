package com.davidp799.patcotoday;

import static com.davidp799.patcotoday.SettingsActivity.PREFERENCE_NOT_FOUND;
import static com.davidp799.patcotoday.SettingsActivity.PREF_DEVICE_THEME;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {
    /* Initialize onCreate */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        // Set Application Theme
        bindThemePrefAction();
    }
    /* Set Application Theme and Adjust SharedPreferences */
    private void bindThemePrefAction() {
        Preference darkThemePref = findPreference(PREF_DEVICE_THEME);

        // Verify Theme Preference
        assert darkThemePref != null : PREFERENCE_NOT_FOUND;

        // Change Theme on Preference Change
        darkThemePref.setOnPreferenceChangeListener((preference, newValue) -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor sharedPrefsEdit = prefs.edit();

            // Change Device Theme and Apply to SharedPreferences
            if (String.valueOf(darkThemePref).equals("Light")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                sharedPrefsEdit.putString("deviceTheme", "Light");
                sharedPrefsEdit.apply();
            } else if (String.valueOf(darkThemePref).equals("Dark")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                sharedPrefsEdit.putString("deviceTheme", "Dark");
                sharedPrefsEdit.apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                sharedPrefsEdit.putString("deviceTheme", "Follow device theme");
                sharedPrefsEdit.apply();
            } return true;
        });

    }
}
