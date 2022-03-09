package com.davidp799.patcotoday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.material.appbar.MaterialToolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.davidp799.patcotoday.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    /* Initialize onCreate */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = this.getWindow();

        // Shared Preferences
        SharedPreferences sharedPreferences = getSharedPreferences("com.davidp799.patcotoday_preferences", MODE_PRIVATE);
        String currentTheme = sharedPreferences.getString("deviceTheme", "");

        // User Interface
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (currentTheme.equals("Dark")) { // application theme
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (currentTheme.equals("Light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); }

        if (Build.VERSION.SDK_INT >= 23) { // status bar and navigation bar colors
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.transparent));
        } else {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.opaque_black));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.opaque_black));
        }

        // Bottom Navigation View //
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
    /* Initialize Settings Button for Top App Bar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }
    /* Open Settings Activity on Button Click */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } return super.onOptionsItemSelected(item);
    }
}