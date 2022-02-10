package com.davidp799.patcotoday;

import static com.davidp799.patcotoday.SettingsActivity.PREFERENCE_NOT_FOUND;
import static com.davidp799.patcotoday.SettingsActivity.PREF_DEVICE_THEME;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        bindThemePrefAction();
    }
    private void bindThemePrefAction() {
        Preference darkThemePref = findPreference(PREF_DEVICE_THEME);
        assert darkThemePref != null : PREFERENCE_NOT_FOUND;
        darkThemePref.setOnPreferenceChangeListener((preference, newValue) -> {
            getActivity().recreate();
            // Allow preference change
            return true;
        });
    }
}
