package com.davidp799.patcotoday;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {
    // Initialize Static Variables

    /* Initialize onCreate */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Window window = this.getWindow();

        // User Interface
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false); // edge-to-edge
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar); // top app bar
        setSupportActionBar(topAppBar); // support app bar
        if (Build.VERSION.SDK_INT >= 23) { // status bar and navigation bar colors
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.transparent));
        } else {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.opaque_black));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.opaque_black));
        }
        if(findViewById(R.id.settings_fragment_container) != null) { // open settings fragment
            if(savedInstanceState == null) {
                getFragmentManager().beginTransaction().add(R.id.settings_fragment_container, new SettingsFragment()).commit();
            }
        }
    }
    /* Exit to MainActivity on Back Arrow Click */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } return super.onOptionsItemSelected(item);
    }
    /* Manually call restart() when theme changed */

}