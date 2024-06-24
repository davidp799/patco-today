package com.davidp799.patcotoday.ui.schedules

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.davidp799.patcotoday.R
import com.davidp799.patcotoday.databinding.FragmentSchedulesBinding
import com.davidp799.patcotoday.ui.BottomSheetListView
import com.davidp799.patcotoday.utils.Arrival
import com.davidp799.patcotoday.utils.ConvertPDF
import com.davidp799.patcotoday.utils.GetSpecial
import com.davidp799.patcotoday.utils.JpgToPdf
import com.davidp799.patcotoday.utils.ParsePDF
import com.davidp799.patcotoday.utils.Trip
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SchedulesFragment : Fragment() {
    // View Binding & Data Store
    private var _binding: FragmentSchedulesBinding? = null
    private val binding get() = _binding!!
    // ViewModel
    private val viewModel: SchedulesViewModel by viewModels()
    // Shared Preferences
    private val preferencesName = "com.davidp799.patcotoday_preferences"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    // UI Elements
    private lateinit var arrivalsListView: ListView
    private lateinit var arrivalsShimmerFrameLayout: ShimmerFrameLayout
    private lateinit var specialAboutShimmerFrameLayout: ShimmerFrameLayout
    private lateinit var specialShimmerFrameLayout: ShimmerFrameLayout
    private lateinit var specialViewButton: Button
    private lateinit var stationsArrayAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSchedulesBinding.inflate(inflater, container, false)
        sharedPreferences =
            requireActivity().getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        val root: View = binding.root
        initLayoutElements(root)

        lifecycleScope.launch(Dispatchers.IO) {
            checkInternetBackgroundTask(requireContext())
            updateListViewBackgroundTask(viewModel.fromIndex, viewModel.toIndex)
            checkSpecialBackgroundTask(requireContext(), root)
        }

        // Initialize station selector elements
        stationsArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, viewModel.stationOptions)
        val fromTextView = initStationSelectorTextView(root, R.id.fromTextView, false)
        val toTextView = initStationSelectorTextView(root, R.id.toTextView, true)
        initReverseStationsButton(root, fromTextView, toTextView)
        return root
    }
    override fun onResume() {
        super.onResume()
        val root: View = binding.root
        val fromTextView =
            root.findViewById<AutoCompleteTextView>(R.id.fromTextView)
        val toTextView =
            root.findViewById<AutoCompleteTextView>(R.id.toTextView)
        val stationsArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            viewModel.stationOptions
        )
        fromTextView.setText(viewModel.fromString)
        fromTextView.setAdapter(stationsArrayAdapter)
        toTextView.setText(viewModel.toString)
        toTextView.setAdapter(stationsArrayAdapter)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // Initialize Reverse Stations Button and listen for selection
    private fun initReverseStationsButton(
        view: View, fromTextView: AutoCompleteTextView, toTextView: AutoCompleteTextView
    ) {
        val button = view.findViewById<ImageButton>(R.id.reverseStationsButton)
        button.setOnClickListener {
            // Show loading animations
            arrivalsShimmerFrameLayout.visibility = View.VISIBLE
            specialAboutShimmerFrameLayout.visibility = View.VISIBLE
            specialShimmerFrameLayout.visibility = View.VISIBLE
            // Rotate reverse icon
            button.rotation = 0f
            val degrees = if (viewModel.isReversed) -180f else 180f
            button.animate().setDuration(180).rotationBy(degrees).start()
            viewModel.isReversed = !viewModel.isReversed
            // Store 'from' values temporarily and swap with 'to' values
            val initialFromIndex = viewModel.fromIndex
            val initialFromString = viewModel.fromString
            viewModel.fromIndex = viewModel.toIndex
            viewModel.fromString = viewModel.toString
            viewModel.toIndex = initialFromIndex
            viewModel.toString = initialFromString
            // Save new values to shared preferences
            sharedPreferencesEditor.putString("last_source", viewModel.fromString)
            sharedPreferencesEditor.putString("last_dest", viewModel.toString)
            sharedPreferencesEditor.apply()

            lifecycleScope.launch(Dispatchers.IO) {
                updateListViewBackgroundTask(viewModel.fromIndex, viewModel.toIndex)
                checkSpecialBackgroundTask(requireContext(), view)
            }
            arrivalsListView.adapter =
                SchedulesListAdapter(
                    context,
                    R.layout.adapter_view_layout,
                    viewModel.schedulesArrayList,
                    0
                )
            stationsArrayAdapter = ArrayAdapter(
                requireContext(),
                R.layout.dropdown_item,
                viewModel.stationOptions
            )
            fromTextView.setText(viewModel.stationOptions[viewModel.fromIndex])
            fromTextView.dismissDropDown()
            toTextView.setText(viewModel.stationOptions[viewModel.toIndex])
            toTextView.dismissDropDown()
            sharedPreferencesEditor.putString("last_source", viewModel.fromString)
            sharedPreferencesEditor.putString("last_dest", viewModel.toString)
            sharedPreferencesEditor.apply()
            fromTextView.setAdapter(stationsArrayAdapter)
            toTextView.setAdapter(stationsArrayAdapter)
        }
    }
    // Initialize Station Selector AutoCompleteTextViews and listen for selection
    private fun initStationSelectorTextView(view: View, resourceId: Int, resourceType: Boolean): AutoCompleteTextView {
        val textView = view.findViewById<AutoCompleteTextView>(resourceId)
        textView.setText(if (resourceType) viewModel.toString else viewModel.fromString)
        textView.setAdapter(stationsArrayAdapter)
        textView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                // Show loading animations
                arrivalsShimmerFrameLayout.visibility = View.VISIBLE
                specialShimmerFrameLayout.visibility = View.VISIBLE
                // Update ViewModel and SharedPreferences based on resourceType
                if (resourceType) {
                    viewModel.toIndex = position
                    viewModel.toString = viewModel.stationOptions[viewModel.toIndex]
                    sharedPreferencesEditor.putString("last_dest", viewModel.toString).apply()
                } else {
                    viewModel.fromIndex = position
                    viewModel.fromString = viewModel.stationOptions[viewModel.fromIndex]
                    sharedPreferencesEditor.putString("last_source", viewModel.fromString).apply()
                }
                // Update listView and special schedules status in background
                lifecycleScope.launch(Dispatchers.IO) {
                    updateListViewBackgroundTask(viewModel.fromIndex, viewModel.toIndex)
                    checkSpecialBackgroundTask(requireContext(), view)
                }
                // Update listView adapter
                arrivalsListView.adapter =
                    SchedulesListAdapter(
                        context,
                        R.layout.adapter_view_layout,
                        viewModel.schedulesArrayList,
                        0
                    )
            }
        return textView
    }
    // Initialize Layout Elements and set View Model values from Shared Preferences
    private fun initLayoutElements(view: View) {
        // set viewModel values from shared preferences
        viewModel.automaticDownloads = sharedPreferences
            .getBoolean("download_on_mobile_data", true)
        viewModel.fromString = sharedPreferences
            .getString("last_source", "Lindenwold").toString()
        viewModel.toString = sharedPreferences
            .getString("last_dest", "15-16th & Locust").toString()
        viewModel.fromIndex = viewModel
            .stationOptions.indexOf(viewModel.fromString)
        viewModel.toIndex = viewModel
            .stationOptions.indexOf(viewModel.toString)
        // initialize layout elements
        arrivalsListView =
            view.findViewById(R.id.arrivalsListView)
        arrivalsShimmerFrameLayout =
            view.findViewById(R.id.arrivalsShimmerFrameLayout)
        specialAboutShimmerFrameLayout =
            view.findViewById(R.id.specialAboutShimmerFrameLayout)
        specialShimmerFrameLayout =
            view.findViewById(R.id.specialShimmerFrameLayout)
        specialViewButton =
            view.findViewById(R.id.specialScheduleViewButton)
        specialViewButton.isEnabled = false

    }
    // Scroll to next arrival in ListView
    private fun scrollToNext(arrivalsArrayList: ArrayList<Arrival>): Int {
        val date = Date()
        val timeFormat = SimpleDateFormat("h:mm aa", Locale.US)
        val timeFormatDate = timeFormat.format(date)
        var scrollIndex = 0
        for (i in 0 until arrivalsArrayList.size) {
            val arrivalTime = arrivalsArrayList[i].arrivalTime.toString()
            try {
                if (arrivalTime == "CLOSED") {
                    Log.d("scrollToNext", "CLOSED, skipping to next")
                } else if (!timeFormat.parse(timeFormatDate)!!.after(timeFormat.parse(arrivalTime))) {
                    break
                }
                scrollIndex = i + 1
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return scrollIndex
    }
    // Check for special schedules in the background
    private suspend fun checkSpecialBackgroundTask(
        context: Context,
        view: View
    ) {
        logThread("checkSpecialBackgroundTask")
        val internetStatus = checkInternet(context)
        setStatusOnMainThread("internetStatus", internetStatus)
        if (internetStatus) {
            val specialStatus = checkSpecial()
            setStatusOnMainThread("specialStatus", specialStatus)
            if (specialStatus) {
                val isMobileData = isMobileData(context)
                var downloadStatus = false
                val existingPdfFilePath: String
                val dataDirectory = context.filesDir.absolutePath + "/data/"

                if (File(dataDirectory + "special/special0.pdf").exists()) {
                    val existingPdfFile = File(dataDirectory + "special/special0.pdf")
                    val lastModifiedDateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(
                            existingPdfFile.lastModified()
                        ),
                        TimeZone.getDefault().toZoneId()
                    )
                    val currentDateTime = LocalDateTime.now()

                    if (lastModifiedDateTime > currentDateTime.minusHours(1)) {
                        existingPdfFilePath = existingPdfFile.absolutePath
                        downloadStatus = true
                    } else {
                        Log.d("[fileCheck]", "deleting existing pdf file")
                        existingPdfFile.delete()
                        existingPdfFilePath = ""
                    }
                } else {
                    Log.d("[fileCheck]", "no existing pdf files found")
                    existingPdfFilePath = ""
                }

                if (isMobileData && viewModel.automaticDownloads && !downloadStatus) {
                    Log.d("[isMobileDataCheck]", "true && true")
                    downloadStatus = downloadSpecial()
                } else if (isMobileData && !viewModel.automaticDownloads && !downloadStatus) {
                    Log.d("[isMobileDataCheck]", "true && false")
                    withContext (Main) {
                        configureBottomSheet(view, true)
                        val specialSchedulesTextView =
                            view.findViewById<TextView>(R.id.specialScheduleAbout)
                        if (viewModel.specialText.size > 0) {
                            specialSchedulesTextView.text = viewModel.specialText[0]
                        }
                        specialAboutShimmerFrameLayout.hideShimmer()
                        specialShimmerFrameLayout.visibility = View.GONE
                    }

                } else if (!isMobileData && !downloadStatus) {
                    Log.d("[isMobileDataCheck]", "false && null")
                    downloadStatus = downloadSpecial()
                }

                if (downloadStatus || (existingPdfFilePath != "")) {
                    val convertStatus = convertSpecial()
                    if (convertStatus) {
                        val parseStatus = parseSpecial(viewModel.fromIndex, viewModel.toIndex)
                        if (parseStatus) {
                            val specialArrivalsArrayList =
                                getSpecialArrivalsBackground(viewModel.fromIndex, viewModel.toIndex)
                            withContext (Main) {
                                viewModel.specialSchedulesArrayList.clear()
                                viewModel.specialSchedulesArrayList.addAll(specialArrivalsArrayList)
                                configureBottomSheet(view, true)
                                val specialSchedulesBottomSheetListView =
                                    view.findViewById<BottomSheetListView>(R.id.specialArrivalsBottomSheetListView)
                                val specialSchedulesTextView =
                                    view.findViewById<TextView>(R.id.specialScheduleAbout)
                                if (viewModel.specialText.size > 0) {
                                    specialSchedulesTextView.text = viewModel.specialText[0]
                                }
                                val specialArrayAdapter =
                                    SchedulesListAdapter(
                                        context,
                                        R.layout.adapter_view_layout,
                                        viewModel.specialSchedulesArrayList,
                                        0
                                    )
                                specialSchedulesBottomSheetListView.adapter = specialArrayAdapter
                                specialArrayAdapter.notifyDataSetChanged()
                                specialAboutShimmerFrameLayout.hideShimmer()
                                specialShimmerFrameLayout.visibility = View.GONE
                            }
                        }
                    }
                }

            } else {
                configureBottomSheetOnMainThread(view, false, specialAboutShimmerFrameLayout, specialShimmerFrameLayout)
            }
        } else {
            configureBottomSheetOnMainThread(view, false, specialAboutShimmerFrameLayout, specialShimmerFrameLayout)
        }
    }
    // Logic function for checking for special schedules
    private fun checkSpecial(): Boolean {
        return try {
            val doc = Jsoup.connect("https://www.ridepatco.org/schedules/schedules.asp").get()
            val getSpecial = GetSpecial(doc)
            viewModel.specialURLs.addAll(getSpecial.url)
            viewModel.specialTexts.addAll(getSpecial.text)
            if (getSpecial.text.size > 0) {
                try {
                    val split = getSpecial.text[0].split("from ")[1].split("-")
                    for (i in split.indices) {
                        if (split[i].contains("AM")) {
                            val value = split[i].replace("AM", " A")
                            viewModel.specialFromToTimes.add(value)
                        } else if (split[i].contains("PM")) {
                            val value = split[i].replace("PM", " P")
                            viewModel.specialFromToTimes.add(value)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("[checkSpecial]", "unknown duration for special schedules")
                    viewModel.specialFromToTimes.add("Various Times")
                }
            }
            viewModel.specialURLs.size > 0
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    // Logic function for downloading special schedules
    private fun downloadSpecial(): Boolean {
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        try {
            val currentMillis = System.currentTimeMillis()
            var filePath= viewModel.directory + "special/special0"
            try {
                val url = URL(viewModel.specialURLs[0])
                filePath += if (url.toString().contains(".jpg")) {
                    ".jpg"
                } else {".pdf"}
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    Log.d(
                        "[downloadPDF]",
                        "Server ResponseCode="
                                + connection.responseCode.toString()
                                + " ResponseMessage="
                                + connection.responseMessage
                    )
                }
                input = connection.inputStream
                Log.d("[downloadPDF] ", "file download successful")
                val newFile = File(filePath)
                newFile.parentFile!!.mkdirs()
                newFile.createNewFile()
                newFile.setLastModified(currentMillis)
                output = FileOutputStream(filePath)
                val data = ByteArray(4096)
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    output.write(data, 0, count)
                }
                viewModel.downloaded = true
                val newPdfFile = File(filePath)
                newPdfFile.setLastModified(currentMillis)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    output?.close()
                    input?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                connection?.disconnect()
            }
            output?.close()
            input?.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    // Logic function for converting special schedules
    private fun convertSpecial(): Boolean {
        return try {
            for (i in 0 until viewModel.specialURLs.size) {
                if (viewModel.specialURLs[i].contains(".jpg")) {
                    JpgToPdf(
                        requireContext(),"special$i.jpg", "special$i.pdf"
                    )
                }
                val convertPDF =
                    ConvertPDF(viewModel.directory + "special/", "special$i.pdf" )
                viewModel.runnableConvertedStrings.add(convertPDF.text)
            }
            viewModel.converted = true
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }
    // Logic function for parsing special schedules
    private fun parseSpecial(source: Int, destination: Int): Boolean {
        return try {
            viewModel.specialWestBound.clear()
            viewModel.specialEastBound.clear()
            viewModel.parsedArrivals.clear()
            for (i in 0 until viewModel.runnableConvertedStrings.size) {
                val parsePDF =
                    ParsePDF(viewModel.runnableConvertedStrings[i])
                viewModel.parsedArrivals.addAll(parsePDF.getArrivalLines(source, destination))
            }
            viewModel.specialWestBound.addAll(viewModel.parsedArrivals[0])
            viewModel.specialEastBound.addAll(viewModel.parsedArrivals[1])
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }
    // Get special arrivals in the background
    private fun getSpecialArrivalsBackground(source: Int, destination: Int): ArrayList<Arrival> {
        return try {
            val travelTime = viewModel.schedules.getTravelTime(source, destination)
            val routeId = viewModel.schedules.getRouteID(source, destination)
            val specialArrivals = arrayListOf(
                viewModel.specialWestBound, viewModel.specialEastBound
            )
            // retrieve list of base data
            var position = 0
            val arrivalsArrayList =
                if (routeId.contains("west")) {
                    specialArrivals[0]
                } else {
                    specialArrivals[1]
                }
            val temp = ArrayList<Trip>()
            try { // check for null
                for (i in arrivalsArrayList.indices) {
                    if (position == 13) {
                        position = 0
                    }
                    if (position == source) {
                        temp.add(arrivalsArrayList[i])
                    }
                    position += 1
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return viewModel.schedules.getFormatArrival(temp, travelTime)
        } catch (e: Error) {
            e.printStackTrace()
            ArrayList()
        }
    }
    // Configure bottom sheet on main thread
    private suspend fun configureBottomSheetOnMainThread(
        view: View, specialStatus: Boolean, specialAboutShimmerFrameLayout: ShimmerFrameLayout, specialShimmerFrameLayout: ShimmerFrameLayout
    ) {
        withContext (Main) {
            configureBottomSheet(view, specialStatus)
            specialAboutShimmerFrameLayout.hideShimmer()
            specialShimmerFrameLayout.visibility = View.GONE
        }
    }
    // Logic function for configuring bottom sheet
    private fun configureBottomSheet(view: View, specialStatus: Boolean ) {
        val bottomSheetLayout
            = view.findViewById<LinearLayout>(R.id.bottom_sheet_layout)
        val bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
            = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetLayout.visibility = View.VISIBLE
        if (!specialStatus || !viewModel.internet || !viewModel.special) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val specialAbout: TextView = view.findViewById(R.id.specialScheduleAbout)
            if (!viewModel.internet) {
                specialAbout.setText(R.string.no_network_connection)
            } else {
                specialAbout.setText(R.string.close_to_schedule)
            }
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val specialAbout: TextView = view.findViewById(R.id.specialScheduleAbout)
            val specialViewButton: Button = view.findViewById(R.id.specialScheduleViewButton)
            specialViewButton.isEnabled = true
            val specialAboutValue = try {
                viewModel.specialTexts[0].split(" | ")[1]
            } catch (e: Exception) {
                viewModel.specialTexts[0]
            }
            specialAbout.text = getString(R.string.special_schedule_about_prefix, specialAboutValue)
            specialViewButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.specialURLs[0])))
            }
        }
    }
    // Check internet connection in the background and set status on main thread
    private suspend fun checkInternetBackgroundTask(context: Context) {
        val internetStatus = checkInternet(context)
        setStatusOnMainThread("internetStatus", internetStatus)
    }
    // Logic function for checking internet connection
    private fun checkInternet(context: Context): Boolean {
        val connectivityManager
            = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
    // Logic function for checking if user is on mobile data connection
    private fun isMobileData(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            val hasCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            val hasWiFi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val hasEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

            return if (hasCellular && !hasWiFi) {
                Log.i("[isMobileData]", "true")
                true
            } else if (hasCellular && !hasEthernet) {
                Log.i("[isMobileData]", "true")
                true
            } else {
                Log.i("[isMobileData]", "false")
                false
            }
        }
        return false
    }
    // Update ListView in the background
    private suspend fun updateListViewBackgroundTask(source: Int, destination: Int) {
        logThread("updateListViewBackgroundTask")
        val arrivalsArrayList = getArrivalsBackgroundTask(source, destination)
        val scrollIndex = scrollToNext(arrivalsArrayList)
        try {
            withContext (Main) {
                viewModel.schedulesArrayList.clear()
                viewModel.schedulesArrayList.addAll(arrivalsArrayList)
                val schedulesAdapter: ArrayAdapter<Arrival> =
                    SchedulesListAdapter(
                        context,
                        R.layout.adapter_view_layout,
                        viewModel.schedulesArrayList,
                        scrollIndex
                    )
                arrivalsListView.adapter = schedulesAdapter
                schedulesAdapter.notifyDataSetChanged()
                arrivalsListView.smoothScrollToPositionFromTop(scrollIndex, 0, 120)
                // TODO: set current arrival listview text as bold
                arrivalsShimmerFrameLayout.visibility = View.GONE
            }

        } catch (e: Error) {
            e.printStackTrace()
        }
    }
    // Get arrivals in the background
    private fun getArrivalsBackgroundTask(sourceId: Int, destinationId: Int): ArrayList<Arrival> {
        return try {
            val travelTime = viewModel.schedules.getTravelTime(sourceId, destinationId)
            val routeId = viewModel.schedules.getRouteID(sourceId, destinationId)
            val schedulesList =
                viewModel.schedules.getSchedulesList(this.context, routeId, sourceId, destinationId)
            return viewModel.schedules.getFormatArrival(schedulesList, travelTime)
        } catch (e: Error) {
            e.printStackTrace()
            ArrayList()
        }
    }
    // Set status for various elements on main thread
    private suspend fun setStatusOnMainThread(key: String, value: Boolean) {
        withContext(Main) {
            when (key) {
                "internetStatus" -> {
                    viewModel.internet = value
                }
                "specialStatus" -> {
                    viewModel.special = value
                    if (!viewModel.special) {
                        Toast.makeText(
                            requireContext(),
                            "No special schedules today",
                            Toast.LENGTH_LONG
                        ).show()
                    } else { }
                }
                else -> {
                    Log.d(
                        "[setStatusOnMainThread]",
                        "unknown key / value pair: $key, $value"
                    )
                }
            }
        }
    }
    // Logger which prints thread name
    private fun logThread(methodName: String){
        Log.d(methodName, Thread.currentThread().name)
    }
}
