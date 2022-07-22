package com.davidp799.patcotoday.ui.schedules

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelLazy
import com.davidp799.patcotoday.R
import com.davidp799.patcotoday.SchedulesListAdapter
import com.davidp799.patcotoday.SettingsActivity
import com.davidp799.patcotoday.databinding.FragmentSchedulesBinding
import com.davidp799.patcotoday.utils.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.*
import java.lang.Math.abs
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class SchedulesFragment : Fragment() {

    private var _binding: FragmentSchedulesBinding? = null

    // Station Data
    private val weekday =
        Calendar.getInstance()[Calendar.DAY_OF_WEEK] - 1 // weekday in java starts on sunday



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val viewModel: SchedulesViewModel by ViewModelLazy(
        SchedulesViewModel::class, {viewModelStore }, { defaultViewModelProviderFactory }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSchedulesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configure UI
        enterTransition = MaterialFadeThrough()

        // implement reverse menu button
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_reverse, menu)

            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.menu_reverse) {
                    return true;
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // shared preferences
        val sharedPreferences = requireActivity().getSharedPreferences(
            "com.davidp799.patcotoday_preferences",
            Context.MODE_PRIVATE
        )

        // Initialize stations options list data
//        val stationOptionsList = listOf(*resources.getStringArray(R.array.stations_list))
        viewModel.fromSelection =
            viewModel.stationOptions.indexOf(sharedPreferences.getString("default_source", "Lindenwold"))
        viewModel.toSelection = viewModel.stationOptions.indexOf(
            sharedPreferences.getString(
                "default_dest",
                "15-16th & Locust"
            )
        )

        // Manage Special Schedules Progress Bar
        val specialProgressBar: LinearProgressIndicator =
            root.findViewById(R.id.specialProgressIndicator)
        specialProgressBar.visibility = View.VISIBLE
        // Special Schedules Title TextView (header)
        val specialHeader = root.findViewById<TextView>(R.id.specialScheduleHeader)

        // Background Activities - internet, special, etc...
        CoroutineScope(Dispatchers.IO).launch {
            checkInternetBackgroundRequest(requireContext())
        }

        // Initialize schedules ListView
        val schedulesListView = root.findViewById<ListView>(R.id.arrivalsListView)
        CoroutineScope(Dispatchers.IO).launch {
            updateListViewBackgroundRequest(viewModel.fromSelection, viewModel.toSelection)
        }
        val schedulesAdapter: ArrayAdapter<Arrival> =
            SchedulesListAdapter(context, R.layout.adapter_view_layout, viewModel.schedulesArrayList)
        schedulesListView.adapter = schedulesAdapter

        val value = scrollToNext(schedulesListView, viewModel.schedulesArrayList)
        schedulesListView.smoothScrollToPositionFromTop(value, 0, 10)

        // Initialize special ListView
        val specialListView = root.findViewById<ListView>(R.id.specialArrivalsListView)
        /* Set progressbar as visible while working */
        CoroutineScope(Dispatchers.IO).launch {
            checkSpecialBackgroundRequest(requireContext(), viewModel.fromSelection, viewModel.toSelection)
        }
        updateSpecialData(specialListView, specialHeader)

        // Initialize array adapter for stations dropdown menu
        val stationsArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            viewModel.stationOptions
        ) // create array adapter
        // Initialize from and to textViews
        val fromAutoCompleteTV =
            root.findViewById<AutoCompleteTextView>(R.id.fromTextView) // get reference to autocomplete text view
        val toAutoCompleteTV =
            root.findViewById<AutoCompleteTextView>(R.id.toTextView) // get reference to autocomplete text view
        // set default text for both textViews
        fromAutoCompleteTV.setText(sharedPreferences.getString("default_source", "Lindenwold"))
        toAutoCompleteTV.setText(sharedPreferences.getString("default_dest", "15-16th & Locust"))

        // connect textViews to stations options arrayAdapter
        fromAutoCompleteTV.setAdapter(stationsArrayAdapter)
        toAutoCompleteTV.setAdapter(stationsArrayAdapter)

        // Listen for station selections: from and to respectively
        fromAutoCompleteTV.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                viewModel.fromSelection = position // set source station to index of selected array position
                // reload listview and scroll to next train
                CoroutineScope(Dispatchers.IO).launch {
                    updateListViewBackgroundRequest(viewModel.fromSelection, viewModel.toSelection)
                }
                val schedulesAdapter: ArrayAdapter<Arrival> =
                    SchedulesListAdapter(context, R.layout.adapter_view_layout, viewModel.schedulesArrayList)
                schedulesListView.adapter = schedulesAdapter
                /* Set progressbar as visible while working */
                specialProgressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    checkSpecialBackgroundRequest(requireContext(), viewModel.fromSelection, viewModel.toSelection)
                }
                updateSpecialData(specialListView, specialHeader)
                specialProgressBar.visibility = View.GONE
            }
        toAutoCompleteTV.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->

                // save from selection as variable
                viewModel.toSelection =
                    position // set destination station to index of selected array position
                // reload listview with new array and adapter and scroll to next train
                CoroutineScope(Dispatchers.IO).launch {
                    updateListViewBackgroundRequest(viewModel.fromSelection, viewModel.toSelection)
                }
                val schedulesAdapter: ArrayAdapter<Arrival> =
                    SchedulesListAdapter(context, R.layout.adapter_view_layout, viewModel.schedulesArrayList)
                schedulesListView.adapter = schedulesAdapter
                /* Set progressbar as visible while working */
                CoroutineScope(Dispatchers.IO).launch {
                    checkSpecialBackgroundRequest(requireContext(), viewModel.fromSelection, viewModel.toSelection)
                }
                updateSpecialData(specialListView, specialHeader)
            }

        /* Initialize Bottom Sheet and Special Loading Parameters */
        val mBottomSheetLayout = root.findViewById<LinearLayout>(R.id.bottom_sheet_layout)
        val sheetBehavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(mBottomSheetLayout)
        val headerArrowImage: ImageView =
            root.findViewById(R.id.bottom_sheet_arrow) // header arrow
        // Header arrow implementation for bottom sheet
        headerArrowImage.setOnClickListener {
            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
        // Implement bottom sheet call
        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                headerArrowImage.rotation = slideOffset * 180
            }
        })
        // Check for internet connection
        if (!viewModel.internet) {
            //Toast.makeText(activity, "No Connection: Working Offline", Toast.LENGTH_SHORT).show()
            sheetBehavior.peekHeight = 0
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED) // hide bottom sheet
        } else {
            //Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show()
            if (!viewModel.special) {
                sheetBehavior.peekHeight = 0
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            } else {
                /* obtain special info from saved array */
                println("im empty...")
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Function used to retrieve arrival times from Schedules class.  */
    private fun getSchedules(source_id: Int, destination_id: Int): ArrayList<Arrival> {
        // Initialize travel duration and routeId for current trip
        val travelTime = viewModel.schedules.getTravelTime(source_id, destination_id)
        val routeId = viewModel.schedules.getRouteID(source_id, destination_id)
        // Retrieve lists of base data
        val serviceIdList = viewModel.schedules.getServiceID(weekday)
        val tripIdList = viewModel.schedules.getTripID(routeId, serviceIdList)
        // Retrieve unformatted list of arrival times
        val schedulesList = viewModel.schedules.getSchedulesList(tripIdList, viewModel.fromSelection)
        // Return formatted list of arrival times for current trip
        return viewModel.schedules.getFormatArrival(schedulesList, travelTime)
    }

    private fun getSpecialSchedules(source_id: Int, destination_id: Int): ArrayList<Arrival> {
        val specialArrivals = ArrayList<ArrayList<String>>()
        specialArrivals.add(viewModel.specialWestBound)
        specialArrivals.add(viewModel.specialEastBound)
        val travelTime = viewModel.schedules.getTravelTime(source_id, destination_id)
        val routeId = viewModel.schedules.getRouteID(source_id, destination_id)
        // retrieve list of base data
        var position = 0
        val theArrivals =
            specialArrivals[abs(routeId - 2)] // i made an oopsie with the routeid
        val temp = ArrayList<String>()
        try { // check for null
            for (i in theArrivals.indices) {
                if (position == 13) {
                    position = 0
                }
                if (position == source_id) {
                    temp.add(theArrivals[i])
                }
                position += 1
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return viewModel.schedules.getFormatArrival(temp, travelTime)
    }

    /** Function used to reload special schedules in bottom sheet.
     * @param listView listView object for schedules
     */
    private fun updateSpecialData(
        listView: ListView,
        header: TextView
    ) {
        /* Set special schedules header to special title */
        if (viewModel.specialText.size > 0) { header.text = viewModel.specialText[0] }
        /* Add formatted arrivals to special listview */
        val specialArrayAdapter =
            SchedulesListAdapter(context, R.layout.adapter_view_layout, viewModel.specialSchedulesArrayList)
        listView.adapter = specialArrayAdapter
        specialArrayAdapter.notifyDataSetChanged()
    }

    private fun scrollToNext(listView: ListView, arrayList: ArrayList<Arrival>): Int {
        val date = Date()
        val timeFormat = SimpleDateFormat("h:mm aa", Locale.US)
        val timeFormatDate = timeFormat.format(date)
        var value = 0
        for (i in 0 until listView.count) {
            val thisArrival = arrayList[i]
            val v = thisArrival.arrivalTime.toString()
            try {
                if (Objects.requireNonNull(timeFormat.parse(timeFormatDate)) == timeFormat.parse(v)) { // curTime == varTime
                    break
                }
                if (Objects.requireNonNull(timeFormat.parse(timeFormat.format(date)))
                        .before(timeFormat.parse(v))
                ) { // curTime < varTime
                    break
                }
                value = i + 1
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return value
    }
    //
    //
    /* Function which manages background tasks using Kotlin coroutines */
    private suspend fun checkSpecialBackgroundRequest(context: Context, source: Int, destination: Int) {
        logThread("\n\n\ncheckSpecialBackgroundRequest Active")
        // step 1: check internet status
        logThread("\nchecking internet status")
        val internetStatus = checkInternet(context)
        setInternetStatusOnMainThread(internetStatus)
        if (internetStatus) {
            // step 2: check special status
            val specialStatus = checkSpecial()
            setSpecialStatusOnMainThread(specialStatus)
            if (specialStatus) {
                // step 3: download special data
                val downloadStatus = downloadSpecial()
                if (downloadStatus) {
                    // step 4: convert pdf data into text
                    val convertStatus = convertSpecial()
                    if (convertStatus) {
                        // step 5: parse text data into special listView content
                        val parseStatus = parseSpecial()
                        if (parseStatus) {
                            // step 6: update main ui thread with special schedule data
                            val specialArrivals = getSpecialArrivalsBackground(source, destination)
                            setSpecialArrivalsOnMainThread(specialArrivals)
                        }
                    }

                }
            }
        }
    }
    private fun checkSpecial(): Boolean {
        logThread("checkSpecialBackground Active")
        try {
            val doc = Jsoup.connect("http://www.ridepatco.org/schedules/schedules.asp").get()
            val getSpecial = GetSpecial(doc)
            viewModel.specialURLs.addAll(getSpecial.url)
            viewModel.specialTexts.addAll(getSpecial.text)
            return viewModel.specialURLs.size > 0
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }
    private suspend fun setSpecialStatusOnMainThread(input: Boolean) {
        withContext (Main) {
            setSpecialStatus(input)
            if (!input) {
                Toast.makeText(requireContext(), "No special schedules today", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setSpecialStatus(input: Boolean){
        viewModel.special = input
    }
    private fun downloadSpecial(): Boolean {
        logThread("downloadSpecialBackground Active")
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        try {
            for (i in 0 until viewModel.specialURLs.size) {
                var filePath= viewModel.directory + "/special/" + "special" + i
                println(viewModel.specialTexts.get(i))
                println(viewModel.specialURLs.get(i))
                try {
                    val url = URL(viewModel.specialURLs[i])
                    if (url.toString().contains(".jpg")) {
                        println("\n\n\nFOUND JPG FILE: $url\n\n\n")
                        filePath += ".jpg"
                    } else {
                        println("\n\n\nFOUND PDF FILE: $url\n\n\n")
                        filePath += ".pdf"
                    }
                    connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        Log.d(
                            "downloadPDF",
                            "Server ResponseCode=" + connection.getResponseCode()
                                .toString() + " ResponseMessage=" + connection.getResponseMessage()
                        )
                    }
                    // download the file

                    input = connection.inputStream
                    Log.d("downloadPDF", "destinationFilePath=$filePath")
                    val newFile = File(filePath)
                    newFile.parentFile.mkdirs()
                    newFile.createNewFile()
                    output = FileOutputStream(filePath)
                    val data = ByteArray(4096)
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        output.write(data, 0, count)
                    }
                    viewModel.downloaded = true
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
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    private fun convertSpecial(): Boolean {
        logThread("convertSpecialBackground Active")
        try {
            for (i in 0 until viewModel.specialURLs.size) {
                if (viewModel.specialURLs.get(i).contains(".jpg")) {
                    println("- Converting: special$i.jpg to pdf")
                    JpgToPdf(requireContext(),"special$i.jpg", "special$i.pdf")
                }
                println("- Converting: special$i.pdf")
                val convertPDF = ConvertPDF(
                    viewModel.directory + "/special/",
                    "special$i.pdf"
                )
                println("- PDF" + i + " Characters = " + convertPDF.text.length)
                viewModel.runnableConvertedStrings.add(convertPDF.text)
            }
            viewModel.converted = true
            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
    }
    private fun parseSpecial(): Boolean {
        logThread("parseSpecialBackground Active")
        try {
            for (i in 0 until viewModel.runnableConvertedStrings.size) {
                println("- Parsing Text$i:")
                println("-----------------------------------------------------------------------")
                val parsePDF = ParsePDF(viewModel.runnableConvertedStrings.get(i))
                viewModel.parsedArrivals.addAll(parsePDF.arrivalLines)
                for (j in 0 until viewModel.parsedArrivals.size) {
                    if (j == 0) {
                        println("Westbound:")
                        println(viewModel.parsedArrivals.get(j))
                    } else {
                        println("Eastbound:")
                        println(viewModel.parsedArrivals.get(j))
                    }
                }
                println("-----------------------------------------------------------------------")
            }
            viewModel.specialWestBound.addAll(viewModel.parsedArrivals.get(0))
            viewModel.specialEastBound.addAll(viewModel.parsedArrivals.get(1))
            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
    }
    private fun getSpecialArrivalsBackground(source: Int, destination: Int): ArrayList<Arrival> {
        logThread("getSpecialArrivalsBackground Active")
        return try {
            getSpecialSchedules(source, destination)
        } catch (e: Error) {
            e.printStackTrace()
            ArrayList()
        }
    }
    private suspend fun setSpecialArrivalsOnMainThread(input: ArrayList<Arrival>) {
        withContext (Main) {
            setSpecialArrivals(input)
        }
    }
    private fun setSpecialArrivals(input: ArrayList<Arrival>){
        viewModel.specialSchedulesArrayList.clear()
        viewModel.specialSchedulesArrayList.addAll(input)
    }
    //
    //
    // Threads which update internet status and get special schedules in background
    private suspend fun checkInternetBackgroundRequest(context: Context) {
        logThread("checkInternetBackgroundRequest Active")
        val status = checkInternet(context)
        setInternetStatusOnMainThread(status)
    }
    private fun checkInternet(context: Context): Boolean {
        logThread("checkInternetBackground Active")
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
    private suspend fun setInternetStatusOnMainThread(input: Boolean) {
        withContext (Main) {
            setInternetStatus(input)
            if (!input) {
                Toast.makeText(requireContext(), "No internet connection. Working offline", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setInternetStatus(input: Boolean){
        viewModel.internet = input
    }
    //
    //
    // Threads which update listView contents in background
    private suspend fun updateListViewBackgroundRequest(source: Int, destination: Int) {
        logThread("backgroundTasksRequest")
        val arrivals = getArrivalsBackground(source, destination)
        setArrivalsOnMainThread(arrivals)
    }
    private fun getArrivalsBackground(source: Int, destination: Int): ArrayList<Arrival> {
        logThread("getSchedulesActive")
        return try {
            getSchedules(source, destination)
        } catch (e: Error) {
            e.printStackTrace()
            ArrayList()
        }
    }
    private suspend fun setArrivalsOnMainThread(input: ArrayList<Arrival>) {
        withContext (Main) {
            setArrivals(input)
        }
    }
    private fun setArrivals(input: ArrayList<Arrival>){
        viewModel.schedulesArrayList.clear()
        viewModel.schedulesArrayList.addAll(input)
    }
    // Logger function for background tasks (or other tasks...)
    private fun logThread(methodName: String){
        println("debug: ${methodName}: ${Thread.currentThread().name}")
    }
}
