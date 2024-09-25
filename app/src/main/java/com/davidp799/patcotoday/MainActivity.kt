package com.davidp799.patcotoday

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.davidp799.patcotoday.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigationrail.NavigationRailView
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {
    // View Binding & Data Store
    private lateinit var binding: ActivityMainBinding
    // ViewModel
    private val viewModel: MainViewModel by viewModels()
    // Shared Preferences
    private val preferencesName = "com.davidp799.patcotoday_preferences"
    private lateinit var sharedPreferences: SharedPreferences
    // Schedule Handling
    private val urlString
        = "https://www.ridepatco.org/developers/PortAuthorityTransitCorporation.zip"
    private val gtfsFileName = "gtfs.zip"
    // Orientation
    private var currentOrientation: Int = Configuration.ORIENTATION_PORTRAIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAppLayout()
        setNavView(setOf( R.id.navigation_schedules, R.id.navigation_map, R.id.navigation_info ))
        lifecycleScope.launch(Dispatchers.IO) {
            checkIfFirstRun()
            requestReview()
            runBackgroundTasks()
        }
        setupNavigationForOrientation(resources.configuration.orientation)
    }
    // Handle orientation change
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        currentOrientation = newConfig.orientation
        setupNavigationForOrientation(newConfig.orientation)
    }
    // Handle back button
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    // Set App Layout
    private fun setAppLayout() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.topAppBar))
        window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent)
    }
    // Set Navigation View
    private fun setNavView(configurationSet: Set<Int>) {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // set up action bar
        val appBarConfiguration = AppBarConfiguration(configurationSet)
        setupActionBarWithNavController(navController, appBarConfiguration)
        // set up bottom navigation and navigation rail
        val bottomNavigationView: BottomNavigationView = binding.navView
        val navigationRailView: NavigationRailView = binding.navigationRail

        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_schedules -> {
                    if (navController.currentDestination?.id == R.id.navigation_schedules) {
                        val bottomSheetLayout
                                = findViewById<LinearLayout>(R.id.bottom_sheet_layout)
                        val bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
                                = BottomSheetBehavior.from(bottomSheetLayout)
                        bottomSheetLayout.visibility = View.VISIBLE
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    } else {
                        navController.navigate(R.id.navigation_schedules)
                    }
                    true
                }
                R.id.navigation_map -> {
                    if (navController.currentDestination?.id != R.id.navigation_map) {
                        navController.navigate(R.id.navigation_map)
                    } else if (navController.currentDestination?.id == R.id.navigation_station_details) {
                        navController.navigateUp() // Go back to map page
                    }
                    true
                }
                R.id.navigation_info -> {
                    if (navController.currentDestination?.id != R.id.navigation_info) {
                        navController.navigate(R.id.navigation_info)
                    }
                    true
                }
                else -> false
            }
        }

        navigationRailView.setupWithNavController(navController)
        navigationRailView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_schedules -> {
                    if (navController.currentDestination?.id == R.id.navigation_schedules) {
                        val bottomSheetLayout
                                = findViewById<LinearLayout>(R.id.bottom_sheet_layout)
                        val bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
                                = BottomSheetBehavior.from(bottomSheetLayout)
                        bottomSheetLayout.visibility = View.VISIBLE
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    } else {
                        navController.navigate(R.id.navigation_schedules)
                    }
                    true
                }
                R.id.navigation_map -> {
                    if (navController.currentDestination?.id != R.id.navigation_map) {
                        navController.navigate(R.id.navigation_map)
                    } else if (navController.currentDestination?.id == R.id.navigation_station_details) {
                        navController.navigateUp() // Go back to map page
                    }
                    true
                }
                R.id.navigation_info -> {
                    if (navController.currentDestination?.id != R.id.navigation_info) {
                        navController.navigate(R.id.navigation_info)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavigationForOrientation(orientation: Int) {
        binding.navView.visibility = if (orientation == Configuration.ORIENTATION_PORTRAIT) View.VISIBLE else View.GONE
        binding.navigationRail.visibility = if (orientation == Configuration.ORIENTATION_LANDSCAPE) View.VISIBLE else View.GONE
    }
    // Set Settings Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_settings, menu)
        return true
    }
    // Handle Settings Menu action
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    // Check if first run
    private fun checkIfFirstRun() {
        Log.d("[checkIfFirstRun]", "started...")
        val prefVersionKeyCode = "version_code"
        val currentVersionCode = BuildConfig.VERSION_CODE
        sharedPreferences = getSharedPreferences(preferencesName, MODE_PRIVATE)
        val sharedPreferencesEditor = sharedPreferences.edit()
        val savedVersionCode = sharedPreferences.getInt(prefVersionKeyCode, -1)

        if ((currentVersionCode > savedVersionCode) || (savedVersionCode.equals(-1))) {
            Log.d(
                "[checkIfFirstRun]",
                "true: current = $currentVersionCode && saved = $savedVersionCode"
            )
            ChangeLogDialogFragment().show(this.supportFragmentManager, ChangeLogDialogFragment.TAG)
            sharedPreferencesEditor.putInt(prefVersionKeyCode, currentVersionCode).apply()
        }
    }
    // Show changelog dialog
    class ChangeLogDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.changelog_title)
                .setMessage(R.string.changelog_message)
                .setPositiveButton(getString(R.string.changelog_ok_button)) { _,_ -> }
                .create()
        companion object {
            const val TAG = "ChangeLogDialog"
        }
    }
    // Request Google Play Store app review
    private fun requestReview() {
        Log.d("[requestReview]", "started...")
        val prefVisitNumber = "visit_number"
        sharedPreferences = getSharedPreferences(preferencesName, MODE_PRIVATE)
        val visitNumber = sharedPreferences.getInt(prefVisitNumber, 0)
        val sharedPreferencesEditor = sharedPreferences.edit()

        if (visitNumber % 10 == 0) {
            val reviewManager = ReviewManagerFactory.create(this)
            val requestReviewFlow = reviewManager.requestReviewFlow()
            requestReviewFlow.addOnCompleteListener {
                if (it.isSuccessful) {
                    val reviewInfo = it.result
                    val reviewFlow = reviewManager.launchReviewFlow(this, reviewInfo)
                    reviewFlow.addOnCompleteListener {
                        sharedPreferencesEditor.putInt(prefVisitNumber, visitNumber + 1).apply()
                    }
                } else {
                    @ReviewErrorCode val reviewErrorCode = (it.exception as? ReviewException)?.errorCode
                    Log.e(
                        "[requestReview]",
                        "reviewErrorCode = $reviewErrorCode"
                    )
                }
            }
        } else {
            sharedPreferencesEditor.putInt(prefVisitNumber, visitNumber + 1).apply()
        }
    }
    // Run background tasks
    private suspend fun runBackgroundTasks() {
        Log.d("[runBackgroundTasks]", "started...")
        cleanUpFiles()
        val internetAvailable = checkInternet(this) // wait until job is done
        setStatusOnMainThread("internetAvailable", internetAvailable)
        if (internetAvailable) {
            setStatusOnMainThread("updateFiles", updateFiles())
            if (!viewModel.updated) {
                val downloaded = downloadZip()
                setStatusOnMainThread("downloadedStatus", downloaded)
                if (downloaded) {
                    val extracted = extractZip()
                    setStatusOnMainThread("extractedStatus", extracted)
                }
            }
        }
    }
    // Set status on main thread for background tasks
    private suspend fun setStatusOnMainThread(key: String, value: Boolean) {
        withContext(Main) {
            when (key) {
                "internetAvailable" -> {
                    viewModel.internet = value
                    if (!viewModel.internet) {
                        Toast.makeText(
                            this@MainActivity,
                            "No internet connection. Working offline",
                            Toast.LENGTH_SHORT
                        ).show()
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
                    viewModel.downloaded = value
                    if (!viewModel.downloaded) {
                        Toast.makeText(
                            this@MainActivity,
                            "Unable to download schedules",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                "extractedStatus" -> {
                    viewModel.extracted = value
                    if (!viewModel.extracted) {
                        Toast.makeText(
                            this@MainActivity,
                            "Unable to configure schedule data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else -> {
                    println("$key, $value")
                }
            }
        }
    }
    // Check internet connection availability
    private fun checkInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val activeNetworkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
    // Clean up old files
    private fun cleanUpFiles() {
        Log.d("[cleanUpFiles]", "started...")
        val dataDirectory = File(filesDir, "data/special/")
        dataDirectory.listFiles()?.filter { it.lastModified() < Date().time }?.forEach { it.delete() }
    }
    // Update data files
    private fun updateFiles(): Boolean {
        Log.d("[updateFiles]", "started...")
        val dataDirectory = this.filesDir.absolutePath + "/data/"
        val dataFiles = resources.getStringArray(R.array.data_files).toList()

        return try {
            // check if new version released
            var updatedCount = 0
            val zipFile = File(dataDirectory + gtfsFileName)
            val lastModified = Date(zipFile.lastModified())
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val latestRelease = dateFormat.parse("11/25/2023")

            if (lastModified < latestRelease) {
                updatedCount++
                Log.d("[updateFiles]", "Files not up to date!")
            } else {
                Log.d("[updateFiles]", "Files up to date...")
                return true
            }
            // Check if all files exist
            var notFound = 0
            for (fileName in dataFiles) {
                val tempFile = File(dataDirectory + fileName)
                if (!tempFile.exists()) notFound++
            }
            notFound += updatedCount
            notFound <= 0
        } catch (e: Exception) {
            Log.e("[updateFiles]", "Files not up to date!")
            false
        }
    }
    // Download GTFS zip file
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
                    "Server ResponseCode="
                            + connection.responseCode
                            + " ResponseMessage="
                            + connection.responseMessage
                )
            }
            // download the file
            input = connection.inputStream
            Log.d(
                "[downloadZipFile]: ",
                "destinationFilePath=$dataDirectory + $gtfsFileName"
            )
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
    // Extract zip file
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
