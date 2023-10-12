package com.davidp799.patcotoday

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {
    private val dataFiles = listOf("agency.txt", "calendar.txt", "calendar_dates.txt",
        "fare_attributes.txt", "fare_rules.txt", "feed_info.txt", "frequencies.txt",
        "routes.txt", "shapes.txt", "stop_times.txt", "stops.txt", "transfers.txt", "trips.txt"
    )
    private val viewModel: MainViewModel by ViewModelLazy(
        MainViewModel::class, {viewModelStore }, { defaultViewModelProviderFactory }
    )
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val preferencesName = "com.davidp799.patcotoday_preferences"
    private val urlString = "http://www.ridepatco.org/developers/PortAuthorityTransitCorporation.zip"
    private val gtfsFileName = "gtfs.zip"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            checkIfFirstRun()
            runBackgroundTasks()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences(preferencesName, MODE_PRIVATE)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.topAppBar))
        window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent)
        when (sharedPreferences.getString("device_theme", "")) {
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
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf( R.id.navigation_schedules, R.id.navigation_map, R.id.navigation_info )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /* Function to check if first run or new version */
    private fun checkIfFirstRun() {
        val prefVersionKeyCode = "version_code"
        val doesNotExist = -1
        val currentVersionCode = BuildConfig.VERSION_CODE
        sharedPreferences = getSharedPreferences(preferencesName, MODE_PRIVATE)
        val savedVersionCode = sharedPreferences.getInt(prefVersionKeyCode, doesNotExist)
        if (currentVersionCode == savedVersionCode) {
            return
        } else if (savedVersionCode == doesNotExist) {
            // TODO: this is a new install (make tasks for new install)
            return
        } else if (currentVersionCode > savedVersionCode) {
            // TODO: this is an upgrade (MAKE TASKS FOR UPGRADE)
            return
        }
        // update shared prefs with current version code
        sharedPreferences.edit().putInt(prefVersionKeyCode, currentVersionCode).apply()
    }
    private suspend fun runBackgroundTasks() {
        print("[backgroundTasksRequest] = Active\n")
        cleanUpFiles()
        val internetAvailable = checkInternet(this) // wait until job is done
        setStatusOnMainThread("internetAvailable", internetAvailable);
        if (internetAvailable) {
            setStatusOnMainThread("updateFiles", updateFiles());
            if (!viewModel.updated) {
                val downloaded = downloadZip()
                setStatusOnMainThread("downloadedStatus", downloaded)
                if (downloaded) {
                    val extracted = extractZip()
                    setStatusOnMainThread("extractedStatus", extracted);
                }
            }
        }
    }
    private suspend fun setStatusOnMainThread(key: String, value: Boolean) {
        withContext(Main) {
            when (key) {
                "internetAvailable" -> {
                    viewModel.internet = value;
                    if (!viewModel.internet) {
                        Toast.makeText(
                            this@MainActivity,
                            "No internet connection. Working offline",
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                }
                "updateFiles" -> {
                    viewModel.updated = value
                }
                "downloadedStatus" -> {
                    Toast.makeText(
                        this@MainActivity,
                        "Downloading new schedules",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.downloaded = value;
                    if (!viewModel.downloaded) {
                        Toast.makeText(
                            this@MainActivity,
                            "Unable to download schedules",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                "extractedStatus" -> {
                    viewModel.extracted = value;
                    if (!viewModel.extracted) {
                        Toast.makeText(
                            this@MainActivity,
                            "Unable to configure schedule data",
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                }
                else -> {
                    println("$key, $value");
                }
            }
        }
    }
    private fun checkInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
    private fun cleanUpFiles(){
        val dataDirectory = this.filesDir.absolutePath + "/data/"
        File(dataDirectory + "special/").walk().forEach {
            val specialPdfFile = File(dataDirectory + "special/" + it)
            val lastModified = Date(specialPdfFile.lastModified())
            if (lastModified < Date() ) {
                it.delete()
            }
        }
    }
    private fun updateFiles(): Boolean {
        val dataDirectory = this.filesDir.absolutePath + "/data/"
        return try {
            // check if new version released
            var updatedCount = 0
            val zipFile = File(dataDirectory + gtfsFileName)
            val lastModified = Date(zipFile.lastModified())
            val latestRelease = Date("08/15/2022")// TODO: Scrape latest release date from web
            if (lastModified < latestRelease) {
                updatedCount++
                print("[updateFiles] State: OUT OF DATE\n")
            } else {
                print("[updateFiles] State: UP TO DATE\n")
            }
            var notFound = 0
            for (fileName in dataFiles) { // Check if all files exist
                val tempFile = File(dataDirectory + fileName)
                if (!tempFile.exists()) notFound++
            }
            notFound += updatedCount
            notFound <= 0
        } catch (e: Exception) {
            println("[updateFiles] Error: Files not up to date!")
            false
        }
    }
    private fun downloadZip(): Boolean {
        val dataDirectory = this.filesDir.absolutePath + "/data/"
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
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
            Log.d("[downloadZipFile]: ", "destinationFilePath=$dataDirectory + $gtfsFileName")
            val newFile = File(dataDirectory+gtfsFileName)
            newFile.parentFile?.mkdirs()
            newFile.createNewFile()
            output = FileOutputStream(dataDirectory+gtfsFileName)
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
    private fun extractZip(): Boolean {
        val dataDirectory = this.filesDir.absolutePath + "/data/"
        val inputStream: InputStream
        val zipInputStream: ZipInputStream
        try {
            val zipFile = File(dataDirectory+gtfsFileName)
            val parentFolder = zipFile.parentFile?.path
            var fileName: String
            inputStream = FileInputStream(dataDirectory+gtfsFileName)
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
