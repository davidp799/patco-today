package com.davidp799.patcotoday;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.davidp799.patcotoday.databinding.ActivityMainBinding;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {
    /* Initialize Variables */
    private static boolean downloaded, extracted, internet, special, updated;
    private static final String directory = "/data/data/com.davidp799.patcotoday/files/data/";
    private final List<String> dataFiles = Arrays.asList( "agency.txt", "calendar.txt", "calendar_dates.txt", "fare_attributes.txt", "fare_rules.txt",
            "feed_info.txt", "frequencies.txt", "routes.txt", "shapes.txt", "stop_times.txt", "stops.txt", "transfers.txt", "trips.txt" );
    private static ConnectivityManager connectivityManager;


    /* Handler for checking network connection */
    Handler downloadHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle downloadBundle = msg.getData();

            downloaded = downloadBundle.getBoolean("MSG_KEY");
        }
    }; Handler extractHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            extracted = msg.getData().getBoolean("MSG_KEY");
            if (extracted) {
                Toast.makeText(MainActivity.this, "Files up to Date", Toast.LENGTH_SHORT).show();
            }
        }
    }; Handler updateHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            updated = msg.getData().getBoolean("MSG_KEY");
        }
    }; Handler internetHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            internet = msg.getData().getBoolean("MSG_KEY");
        }
    };

    /* Initialize onCreate */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Splash screen */
/*        if (Build.VERSION.SDK_INT >= 31) { // set splashscreen if api 31+
            androidx.core.splashscreen.SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        }*/
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = this.getWindow();

        // Check Network in Background Thread
        checkInternet();
        // Check and Update Files
        updateFiles();

        // Shared Preferences
        SharedPreferences sharedPreferences = getSharedPreferences("com.davidp799.patcotoday_preferences", MODE_PRIVATE);
        String currentTheme = sharedPreferences.getString("deviceTheme", "");

        // User Interface
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show home button on left
        }
        topAppBar.setNavigationIcon(R.drawable.ic_baseline_update_24);
        topAppBar.setNavigationContentDescription("Check for updates");


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
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    /* Open Settings Activity on Button Click */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            Toast.makeText(this, "Updating Schedules", Toast.LENGTH_SHORT).show();
            downloadZip("http://www.ridepatco.org/developers/PortAuthorityTransitCorporation.zip", directory + "gtfs.zip"); // download zip file
            extractZip(directory + "gtfs.zip"); // extract file contents
            if (extracted) {
                updated = true;
            } return true;
        }
        return super.onOptionsItemSelected(item);
    } public static boolean getInternet(){
        return internet;
    }
    /* Background Threads: updateFiles, checkInternet, downloadPDF, downloadZip, extractZip */
    public void updateFiles() {
        Runnable updateRunnable = new Runnable() {
            Message updateMessage = updateHandler.obtainMessage();
            Bundle updateBundle = new Bundle();
            @Override
            public void run() {
                try {
                    File fileDir = new File(directory);
                    fileDir.mkdirs();

                    int notFound = 0;
                    for (String fileName : dataFiles) { // Check for data files
                        File tempFile = new File(directory + fileName);
                        if (!tempFile.exists()) notFound++;
                    } if (notFound > 0) {
                        Toast.makeText(MainActivity.this, String.format("E: %s Files Not Found", notFound), Toast.LENGTH_SHORT).show(); // DEBUG REMOVE WHEN FINISHED
                        updated = false;
                    } else {
                        updated = true;
                    }
                } catch (Exception e) {
                    System.out.println("Error: Files not up to date!");
                }
                updateBundle.putBoolean("MSG_KEY", updated);
                updateMessage.setData(updateBundle);
                updateHandler.sendMessage(updateMessage);
            }
        };
        Thread updateBgThread = new Thread(updateRunnable);
        updateBgThread.start();
    }
    public void checkInternet() {
        Runnable internetRunnable = new Runnable() {
            Message internetMessage = internetHandler.obtainMessage();
            Bundle internetBundle = new Bundle();
            @Override
            public void run() {
                try {
                    connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // use new connectivity manager mode if Android N
                        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                            @Override
                            public void onAvailable(@NonNull Network network) {
                                internet = true;
                            }
                            @Override
                            public void onLost(@NonNull Network network) {
                                internet = false;
                            }
                        });
                    } else { // otherwise, ping server
                        try {
                            String command = "ping -c 1 www.ridepatco.org";
                            internet = (Runtime.getRuntime().exec(command).waitFor() == 0);
                        } catch (Exception e) {
                            internet = false;
                        }
                    }
                } catch (Exception e) {
                    internet = false;
                }
                internetBundle.putBoolean("MSG_KEY", internet);
                internetMessage.setData(internetBundle);
                internetHandler.sendMessage(internetMessage);
            }
        };
        Thread internetBgThread = new Thread(internetRunnable);
        internetBgThread.start();
    }
    public void downloadZip(String urlStr, String destinationFilePath) {
        Runnable downloadRunnable = new Runnable() {
            Message downloadMessage = downloadHandler.obtainMessage();
            Bundle downloadBundle = new Bundle();
            @Override
            public void run() {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urlStr);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.d("downloadZipFile", "Server ResponseCode=" + connection.getResponseCode() + " ResponseMessage=" + connection.getResponseMessage());
                    }
                    // download the file
                    input = connection.getInputStream();
                    Log.d("downloadZipFile", "destinationFilePath=" + destinationFilePath);
                    File newFile = new File(destinationFilePath);
                    newFile.getParentFile().mkdirs();
                    newFile.createNewFile();
                    output = new FileOutputStream(destinationFilePath);
                    byte[] data = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                } finally {
                    try {
                        if (output != null) output.close();
                        if (input != null) input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (connection != null) connection.disconnect();
                }
                File f = new File(destinationFilePath);
                Log.d("downloadZipFile", "f.getParentFile().getPath()=" + f.getParentFile().getPath());
                Log.d("downloadZipFile", "f.getName()=" + f.getName().replace(".zip", ""));
            }
        };
        Thread downloadBgThread = new Thread(downloadRunnable);
        downloadBgThread.start();
    } public void extractZip(String filePath) {
        Runnable extractRunnable = new Runnable() {
            Message extractMessage = extractHandler.obtainMessage();
            Bundle extractBundle = new Bundle();
            @Override
            public void run() {
                InputStream is;
                ZipInputStream zis;
                try {
                    File zipfile = new File(filePath);
                    String parentFolder = zipfile.getParentFile().getPath();
                    String filename;
                    is = new FileInputStream(filePath);
                    zis = new ZipInputStream(new BufferedInputStream(is));
                    ZipEntry ze;
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((ze = zis.getNextEntry()) != null) {
                        filename = ze.getName();
                        if (ze.isDirectory()) {
                            File fmd = new File(parentFolder + "/" + filename);
                            fmd.mkdirs();
                            continue;
                        } FileOutputStream fout = new FileOutputStream(
                                parentFolder + "/" + filename);
                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }
                        fout.close();
                        zis.closeEntry();
                    } zis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    extracted = false;
                }
                extracted = true;
            }
        };
        Thread extractBgThread = new Thread(extractRunnable);
        extractBgThread.start();
    }
}