package com.davidp799.patcotoday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.transition.MaterialFadeThrough;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        // Set Status bar & Navigation bar Colors //
        if (Build.VERSION.SDK_INT >= 21) {
            int nightModeFlags =
                    this.getResources().getConfiguration().uiMode &
                            Configuration.UI_MODE_NIGHT_MASK;
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    if (Build.VERSION.SDK_INT >= 31) {
                        window.setStatusBarColor(this.getResources().getColor(R.color.material_dynamic_neutral_variant10));
                        window.setNavigationBarColor(this.getResources().getColor(R.color.gm3_ref_palette_dynamic_neutral10));

                    } else {
                        window.setStatusBarColor(this.getResources().getColor(R.color.blackish));
                        window.setNavigationBarColor(this.getResources().getColor(R.color.blackish));
                    } break;
                case Configuration.UI_MODE_NIGHT_NO:
                    if (Build.VERSION.SDK_INT >= 31) {
                        window.setStatusBarColor(this.getResources().getColor(R.color.material_dynamic_primary95));
                        window.setNavigationBarColor(this.getResources().getColor(R.color.material_dynamic_primary99));
                    } else {
                        window.setStatusBarColor(this.getResources().getColor(R.color.blackish));
                        window.setNavigationBarColor(this.getResources().getColor(R.color.blackish));
                    } break;
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    break;
            }
        }
        // action bar: back button, title
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setElevation(0);
        if(findViewById(R.id.settings_fragment_container) != null) {
            if(savedInstanceState != null) {
                return;
            }
            getFragmentManager().beginTransaction().add(R.id.settings_fragment_container, new SettingsFragment()).commit();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}