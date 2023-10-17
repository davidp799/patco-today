package com.davidp799.patcotoday.ui.schedules

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelLazy
import com.davidp799.patcotoday.R
import com.davidp799.patcotoday.databinding.FragmentSchedulesBinding
import com.davidp799.patcotoday.utils.*
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.jsoup.Jsoup
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.util.*


class SchedulesFragment : Fragment() {
    private var _binding: FragmentSchedulesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SchedulesViewModel by ViewModelLazy(
        SchedulesViewModel::class, {viewModelStore }, { defaultViewModelProviderFactory }
    )
    private lateinit var sharedPreferences: SharedPreferences
    private val preferencesName = "com.davidp799.patcotoday_preferences"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSchedulesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        sharedPreferences =
            requireActivity().getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val sharedPreferencesEditor = sharedPreferences.edit()

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

        val fromTextView =
            root.findViewById<AutoCompleteTextView>(R.id.fromTextView)
        val toTextView =
            root.findViewById<AutoCompleteTextView>(R.id.toTextView)
        var stationsArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, viewModel.stationOptions)
        val reverseStationsButton =
            root.findViewById<ImageButton>(R.id.reverseStationsButton)
        val arrivalsListView =
            root.findViewById<ListView>(R.id.arrivalsListView)
        val arrivalsShimmerFrameLayout: ShimmerFrameLayout =
            root.findViewById(R.id.arrivalsShimmerFrameLayout)
        val specialAboutShimmerFrameLayout: ShimmerFrameLayout =
            root.findViewById(R.id.specialAboutShimmerFrameLayout)
        val specialShimmerFrameLayout: ShimmerFrameLayout =
            root.findViewById(R.id.specialShimmerFrameLayout)
        val specialViewButton: Button =
            root.findViewById(R.id.specialScheduleViewButton)

        fromTextView.setText(viewModel.fromString)
        fromTextView.setAdapter(stationsArrayAdapter)
        toTextView.setText(viewModel.toString)
        toTextView.setAdapter(stationsArrayAdapter)
        specialViewButton.isEnabled = false

        enterTransition = MaterialFadeThrough()

        CoroutineScope(Dispatchers.IO).launch {
            checkInternetBackgroundTask(
                requireContext()
            )
            updateListViewBackgroundTask(
                viewModel.fromIndex,
                viewModel.toIndex,
                arrivalsListView,
                arrivalsShimmerFrameLayout
            )
            checkSpecialBackgroundTask(
                requireContext(),
                viewModel.fromIndex,
                viewModel.toIndex,
                specialAboutShimmerFrameLayout,
                specialShimmerFrameLayout,
                root
            )
        }

        fromTextView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.fromIndex = position
                viewModel.fromString = viewModel.stationOptions[viewModel.fromIndex]
                sharedPreferencesEditor.putString("last_source", viewModel.fromString).apply()
                arrivalsShimmerFrameLayout.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    updateListViewBackgroundTask(
                        viewModel.fromIndex, viewModel.toIndex,
                        arrivalsListView, arrivalsShimmerFrameLayout
                    )
                }
                arrivalsListView.adapter =
                    SchedulesListAdapter(
                        context,
                        R.layout.adapter_view_layout,
                        viewModel.schedulesArrayList,
                        0
                    )
                specialShimmerFrameLayout.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    checkSpecialBackgroundTask(
                        requireContext(),
                        viewModel.fromIndex,
                        viewModel.toIndex,
                        specialAboutShimmerFrameLayout,
                        specialShimmerFrameLayout,
                        root
                    )
                }
            }

        toTextView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.toIndex = position
                viewModel.toString = viewModel.stationOptions[viewModel.toIndex]
                sharedPreferencesEditor.putString("last_dest", viewModel.toString).apply()
                arrivalsShimmerFrameLayout.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    updateListViewBackgroundTask(
                        viewModel.fromIndex, viewModel.toIndex,
                        arrivalsListView, arrivalsShimmerFrameLayout
                    )
                }
                arrivalsListView.adapter =
                    SchedulesListAdapter(
                        context,
                        R.layout.adapter_view_layout,
                        viewModel.schedulesArrayList,
                        0
                    )
                specialAboutShimmerFrameLayout.visibility = View.VISIBLE
                specialShimmerFrameLayout.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    checkSpecialBackgroundTask(
                        requireContext(),
                        viewModel.fromIndex,
                        viewModel.toIndex,
                        specialAboutShimmerFrameLayout,
                        specialShimmerFrameLayout,
                        root
                    )
                }
            }

        reverseStationsButton.setOnClickListener {
            if (viewModel.isReversed) {
                reverseStationsButton.animate().setDuration(180).rotationBy(-180f).start()
                viewModel.isReversed = false
            } else {
                reverseStationsButton.animate().setDuration(180).rotationBy(180f).start()
                viewModel.isReversed = true
            }
            val tempIndex = viewModel.fromIndex
            val tempString = viewModel.fromString
            viewModel.fromIndex = viewModel.toIndex
            viewModel.fromString = viewModel.toString
            sharedPreferencesEditor.putString("last_source", viewModel.fromString)
            viewModel.toIndex = tempIndex
            viewModel.toString = tempString
            sharedPreferencesEditor.putString("last_dest", viewModel.toString)
            sharedPreferencesEditor.apply()

            arrivalsShimmerFrameLayout.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                updateListViewBackgroundTask(
                    viewModel.fromIndex, viewModel.toIndex,
                    arrivalsListView, arrivalsShimmerFrameLayout
                )
            }
            arrivalsListView.adapter =
                SchedulesListAdapter(
                    context,
                    R.layout.adapter_view_layout,
                    viewModel.schedulesArrayList,
                    0
                )
            specialAboutShimmerFrameLayout.visibility = View.VISIBLE
            specialShimmerFrameLayout.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                checkSpecialBackgroundTask(
                    requireContext(),
                    viewModel.fromIndex,
                    viewModel.toIndex,
                    specialAboutShimmerFrameLayout,
                    specialShimmerFrameLayout,
                    root
                )
            }

            stationsArrayAdapter = ArrayAdapter(
                requireContext(),
                R.layout.dropdown_item,
                viewModel.stationOptions
            )
            fromTextView.setText(viewModel.stationOptions[viewModel.fromIndex])
            toTextView.setText(viewModel.stationOptions[viewModel.toIndex])
            sharedPreferencesEditor.putString("last_source", viewModel.fromString)
            sharedPreferencesEditor.putString("last_dest", viewModel.toString)
            sharedPreferencesEditor.apply()
            fromTextView.setAdapter(stationsArrayAdapter)
            toTextView.setAdapter(stationsArrayAdapter)
        }
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

    private fun scrollToNext(arrivalsArrayList: ArrayList<Arrival>): Int {
        val date = Date()
        val timeFormat = SimpleDateFormat("h:mm aa", Locale.US)
        val timeFormatDate = timeFormat.format(date)
        var scrollIndex = 0
        for (i in 0 until arrivalsArrayList.size) {
            val arrivalTime = arrivalsArrayList[i].arrivalTime.toString()
            try {
                if (!timeFormat.parse(timeFormatDate)!!.after(timeFormat.parse(arrivalTime))) {
                    break
                }
                scrollIndex = i + 1
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return scrollIndex
    }

    private suspend fun checkSpecialBackgroundTask(
        context: Context,
        source: Int,
        destination: Int,
        specialAboutShimmerFrameLayout: ShimmerFrameLayout,
        specialShimmerFrameLayout: ShimmerFrameLayout,
        view: View
    ) {
        logThread("checkSpecialBackgroundTask")
        val internetStatus = checkInternet(context)
        setStatusOnMainThread("internetStatus", internetStatus)
        if (internetStatus) {
            val specialStatus = checkSpecial()
            setStatusOnMainThread("specialStatus", specialStatus)
            if (specialStatus) {
                var continueDownload = true
                var downloadStatus = false
                var existingPdfFilePath = ""
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
                        existingPdfFilePath = existingPdfFile.absolutePath // TODO: use old files instead of downloading new
                        continueDownload = false
                    } else {
                        Log.d("[fileCheck]", "deleting existing pdf file")
                        existingPdfFile.delete()
                        continueDownload = true
                    }
                } else {
                    Log.d("[fileCheck]", "no existing pdf files found")
                }
                if (viewModel.automaticDownloads && continueDownload) {
                    downloadStatus = downloadSpecial()
                } else {
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
                }
                if (downloadStatus || (existingPdfFilePath != "")) {
                    val convertStatus = convertSpecial()
                    if (convertStatus) {
                        val parseStatus = parseSpecial()
                        if (parseStatus) {
                            val specialArrivalsArrayList =
                                getSpecialArrivalsBackground(source, destination)
                            withContext (Main) {
                                viewModel.specialSchedulesArrayList.clear()
                                viewModel.specialSchedulesArrayList.addAll(specialArrivalsArrayList)
                                configureBottomSheet(view, true)
                                val specialSchedulesListView =
                                    view.findViewById<ListView>(R.id.specialArrivalsListView)
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
                                specialSchedulesListView.adapter = specialArrayAdapter
                                specialArrayAdapter.notifyDataSetChanged()
                                specialAboutShimmerFrameLayout.hideShimmer()
                                specialShimmerFrameLayout.visibility = View.GONE
                            }
                        }
                    }
                }
            } else {
                configureBottomSheetOnMainThread(view, false, specialShimmerFrameLayout)
            }
        } else {
            configureBottomSheetOnMainThread(view, false, specialShimmerFrameLayout)
        }
    }
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
    private fun downloadSpecial(): Boolean {
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        try {
            for (i in 0 until viewModel.specialURLs.size) {
                var filePath= viewModel.directory + "special/" + "special" + i
                try {
                    val url = URL(viewModel.specialURLs[i])
                    filePath += if (url.toString().contains(".jpg")) {
                        ".jpg"
                    } else {
                        ".pdf"
                    }
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
                    newFile.setLastModified(System.currentTimeMillis())
                    output = FileOutputStream(filePath)
                    val data = ByteArray(4096)
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        output.write(data, 0, count)
                    }
                    viewModel.downloaded = true
                    val newPdfFile = File(filePath)
                    newPdfFile.setLastModified(System.currentTimeMillis())
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
            }
            output?.close()
            input?.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

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

    private fun parseSpecial(): Boolean {
        return try {
            for (i in 0 until viewModel.runnableConvertedStrings.size) {
                val parsePDF =
                    ParsePDF(viewModel.runnableConvertedStrings[i])
                viewModel.parsedArrivals.addAll(parsePDF.arrivalLines)
            }
            viewModel.specialWestBound.addAll(viewModel.parsedArrivals[0])
            viewModel.specialEastBound.addAll(viewModel.parsedArrivals[1])
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun getSpecialArrivalsBackground(source: Int, destination: Int): ArrayList<Arrival> {
        return try {
            val specialArrivals = arrayListOf(
                viewModel.specialWestBound, viewModel.specialEastBound
            )
            val travelTime = viewModel.schedules.getTravelTime(source, destination)
            val routeId = viewModel.schedules.getRouteID(
                viewModel.schedules.tripId, source, destination
            )
            // retrieve list of base data
            var position = 0
            val arrivalsArrayList =
                if (routeId.contains("west")) {
                    specialArrivals[0]
                } else {
                    specialArrivals[1]
                }
            val temp = ArrayList<String>()
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
    private suspend fun configureBottomSheetOnMainThread(
        view: View, specialStatus: Boolean, specialShimmerFrameLayout: ShimmerFrameLayout
    ) {
        withContext (Main) {
            configureBottomSheet(view, specialStatus)
            specialShimmerFrameLayout.visibility = View.GONE
        }
    }

    private fun configureBottomSheet(view: View, specialStatus: Boolean ) {
        val bottomSheetLayout
            = view.findViewById<LinearLayout>(R.id.bottom_sheet_layout)
        val bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
            = BottomSheetBehavior.from(bottomSheetLayout)
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
            specialAbout.text = specialAboutValue
            specialViewButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.specialURLs[0])))
            }
        }
    }

    private suspend fun checkInternetBackgroundTask(context: Context) {
        val internetStatus = checkInternet(context)
        setStatusOnMainThread("internetStatus", internetStatus)
    }

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

    private suspend fun updateListViewBackgroundTask(
        source: Int, destination: Int,
        listView: ListView, arrivalsShimmerFrameLayout: ShimmerFrameLayout
    ) {
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
                listView.adapter = schedulesAdapter
                schedulesAdapter.notifyDataSetChanged()
                listView.smoothScrollToPositionFromTop(scrollIndex, 0, 120)
                // TODO: set current arrival listview text as bold
                arrivalsShimmerFrameLayout.visibility = View.GONE
            }

        } catch (e: Error) {
            e.printStackTrace()
        }
    }
    private fun getArrivalsBackgroundTask(sourceId: Int, destinationId: Int): ArrayList<Arrival> {
        return try {
            val travelTime = viewModel.schedules.getTravelTime(sourceId, destinationId)
            val tripId = viewModel.schedules.tripId
            val routeId = viewModel.schedules.getRouteID(tripId, sourceId, destinationId)
            val schedulesList =
                viewModel.schedules.getSchedulesList(this.context, routeId, viewModel.fromIndex)
            return viewModel.schedules.getFormatArrival(schedulesList, travelTime)
        } catch (e: Error) {
            e.printStackTrace()
            ArrayList()
        }
    }
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
    private fun logThread(methodName: String){
        Log.d(methodName, Thread.currentThread().name)
    }
}
