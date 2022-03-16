package com.davidp799.patcotoday;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {
    /* Initialize static variables */
    public static final String PREFERENCE_NOT_FOUND = "preference not found";
    public static final String PREF_DEVICE_THEME = "deviceTheme";

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
                sharedPrefsEdit.putString("deviceTheme", "Light");
                sharedPrefsEdit.apply();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            } else if (String.valueOf(darkThemePref).equals("Dark")) {
                sharedPrefsEdit.putString("deviceTheme", "Dark");
                sharedPrefsEdit.apply();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            } else {
                sharedPrefsEdit.putString("deviceTheme", "Follow device theme");
                sharedPrefsEdit.apply();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

            } return true;
        });

    }

    private void updateView() {

    }
}
