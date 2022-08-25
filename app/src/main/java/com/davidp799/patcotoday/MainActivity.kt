package com.davidp799.patcotoday

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.davidp799.patcotoday.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {
    private val dataFiles = listOf(
        "agency.txt",
        "calendar.txt",
        "calendar_dates.txt",
        "fare_attributes.txt",
        "fare_rules.txt",
        "feed_info.txt",
        "frequencies.txt",
        "routes.txt",
        "shapes.txt",
        "stop_times.txt",
        "stops.txt",
        "transfers.txt",
        "trips.txt"
    )

    private val viewModel: MainViewModel by ViewModelLazy(
        MainViewModel::class, {viewModelStore }, { defaultViewModelProviderFactory }
    )

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Perform background tasks in following order:
        *  1.) Check Internet;
        *  2.) Check if files up to date;
        *  3.) If updated == false, download files;
        *  4.) if downloaded == true, extract files; */
        CoroutineScope(Dispatchers.IO).launch {
            backgroundTasksRequest()
        }

        // Configure Shared Preferences
        val sharedPreferences =
            getSharedPreferences("com.davidp799.patcotoday_preferences", MODE_PRIVATE)
        val currentTheme = sharedPreferences.getString("device_theme", "")

        // Support Material Toolbar
        val window = this.window
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(topAppBar)

        // Add 'home' button to toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // show home button
        topAppBar.setNavigationIcon(R.drawable.ic_update_tt)
        topAppBar.navigationContentDescription = "Check for updates"

        // Set current theme (light/dark/auto)
        when (currentTheme) {
            "1" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            "2" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        // Set status bar and navigation bar colors
        if (Build.VERSION.SDK_INT >= 23) { // status bar and navigation bar colors
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent)
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.opaque_black)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.opaque_black)
        }

        // Bottom Navigation View
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_schedules, R.id.navigation_map, R.id.navigation_info
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    /* Initialize Settings Button for Top App Bar */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    /* Open Activity on Button Click */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        } else if (item.itemId == android.R.id.home) {
            Toast.makeText(this, "Updating Schedules", Toast.LENGTH_SHORT).show()
            // call download zip coroutine here
            // extract file contents
            // check if updated == true
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /* Background Tasks using Kotlin Coroutine: internet, update, download, extract */
    private fun logThread(methodName: String){
        println("debug: ${methodName}: ${Thread.currentThread().name}")
    }

    private fun setInternetStatus(input: Boolean){
        viewModel.internet = input
    }
    private suspend fun setInternetStatusOnMainThread(input: Boolean) {
        withContext (Main) {
            setInternetStatus(input)
            if (!input) {
                Toast.makeText(this@MainActivity, "No internet connection. Working offline", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setUpdatedStatus(input: Boolean){
        viewModel.updated = input
    }
    private suspend fun setUpdatedStatusOnMainThread(input: Boolean) {
        withContext (Main) {
            setUpdatedStatus(input)
        }
    }
    private fun setDownloadedStatus(input: Boolean){
        viewModel.downloaded = input
    }
    private suspend fun setDownloadedStatusOnMainThread(input: Boolean) {
        withContext (Main) {
            Toast.makeText(this@MainActivity, "Downloading new schedules", Toast.LENGTH_LONG).show()
            setDownloadedStatus(input)
            if (!input) {
                Toast.makeText(this@MainActivity, "Unable to download schedules", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setExtractedStatus(input: Boolean){
        viewModel.extracted = input
    }
    private suspend fun setExtractedStatusOnMainThread(input: Boolean) {
        withContext (Main) {
            setExtractedStatus(input)
            if (!input) {
                Toast.makeText(this@MainActivity, "Unable to configure schedule data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun backgroundTasksRequest() {
        logThread("\n*** backgroundTasksRequest Active ***\n")
        val connected = checkInternet(this) // wait until job is done
        setInternetStatusOnMainThread(connected)
        if (connected) {
            setUpdatedStatusOnMainThread(updateFiles())
            if (!viewModel.updated) {
                val downloaded = downloadZip("http://www.ridepatco.org/developers/PortAuthorityTransitCorporation.zip", viewModel.directory + "gtfs.zip")
                setDownloadedStatusOnMainThread(downloaded)
                if (downloaded) {
                    val extracted = extractZip(viewModel.directory + "gtfs.zip")
                    setExtractedStatusOnMainThread(extracted)
                }
            }
        }
    }

    private fun checkInternet(context: Context): Boolean {
        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // if Android M or greater, use NetworkCapabilities to check which network has connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Returns Network object corresponding to active default data network.
            val network = connectivityManager.activeNetwork ?: return false
            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport, or WiFi has connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                // Indicates this network uses a Cellular transport, or Cellular has connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    private fun updateFiles(): Boolean {
        return try {
            // check if new version released
            var updated = 0
            val zipFile = File(viewModel.directory + "gtfs.zip")
            val lastModified = Date(zipFile.lastModified())
            val latestRelease = Date("12/4/2021") // TODO: Scrape latest release date from web
            val formatter = SimpleDateFormat("M/d/yyyy", Locale.US)
            val formattedDateString: String = formatter.format(lastModified)
            print("!!!! Zip modified on $formattedDateString")
            if (lastModified < latestRelease) {
                updated++
                print(" !!!!! FILES OUT OF DATE")
            } else {
                print("!!!!! FILES UP TO DATE")
            }

            // check if files downloaded and extracted
            var notFound = 0
            for (fileName in dataFiles) { // Check for data files
                val tempFile = File(viewModel.directory + fileName)
                if (!tempFile.exists()) notFound++
            }
            notFound += updated
            notFound <= 0
        } catch (e: Exception) {
            println("Error: Files not up to date!")
            false
        }
    }

    private fun downloadZip(urlStr: String?, destinationFilePath: String): Boolean {
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.d(
                    "downloadZipFile",
                    "Server ResponseCode=" + connection.responseCode + " ResponseMessage=" + connection.responseMessage
                )
            }
            // download the file
            input = connection.inputStream
            Log.d("downloadZipFile", "destinationFilePath=$destinationFilePath")
            val newFile = File(destinationFilePath)
            newFile.parentFile?.mkdirs()
            newFile.createNewFile()
            output = FileOutputStream(destinationFilePath)
            val data = ByteArray(4096)
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                output.write(data, 0, count)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            try {
                output?.close()
                input?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            connection?.disconnect()
        }
    }

    private fun extractZip(filePath: String): Boolean {
        val inputStream: InputStream
        val zipInputStream: ZipInputStream
        try {
            val zipFile = File(filePath)
            val parentFolder = zipFile.parentFile?.path
            var fileName: String
            inputStream = FileInputStream(filePath)
            zipInputStream = ZipInputStream(BufferedInputStream(inputStream))
            var zipEntry: ZipEntry?
            val buffer = ByteArray(1024)
            var count: Int
            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                fileName = zipEntry!!.name
                if (zipEntry!!.isDirectory) {
                    val fmd = File("$parentFolder/$fileName")
                    fmd.mkdirs()
                    continue
                }
                val fileOut = FileOutputStream(
                    "$parentFolder/$fileName"
                )
                while (zipInputStream.read(buffer).also { count = it } != -1) {
                    fileOut.write(buffer, 0, count)
                }
                fileOut.close()
                zipInputStream.closeEntry()
            }
            zipInputStream.close()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }
}
