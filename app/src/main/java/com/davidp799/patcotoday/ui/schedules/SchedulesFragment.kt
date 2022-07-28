package com.davidp799.patcotoday.ui.schedules

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
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
import com.davidp799.patcotoday.databinding.FragmentSchedulesBinding
import com.davidp799.patcotoday.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.jsoup.Jsoup
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class SchedulesFragment : Fragment() {
    private var _binding: FragmentSchedulesBinding? = null

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
        val arrivalsProgressBar: LinearProgressIndicator = root.findViewById(R.id.arrivalsProgressBar)
        arrivalsProgressBar.visibility = View.GONE
        val specialViewButton: Button = root.findViewById(R.id.specialScheduleViewButton)
        specialViewButton.visibility = View.GONE

        // shared preferences
        val sharedPreferences = requireActivity().getSharedPreferences(
            "com.davidp799.patcotoday_preferences",
            Context.MODE_PRIVATE
        )
        // Initialize stations options list data
        viewModel.fromSelection =
            viewModel.stationOptions.indexOf(sharedPreferences.getString("default_source", "Lindenwold"))
        viewModel.toSelection =
            viewModel.stationOptions.indexOf(sharedPreferences.getString("default_dest", "15-16th & Locust")
        )

        // Manage Special Schedules Progress Bar
        val specialProgressBar: LinearProgressIndicator =
            root.findViewById(R.id.specialProgressIndicator)
        specialProgressBar.visibility = View.VISIBLE

        // Background Activities - internet, special, etc...
        CoroutineScope(Dispatchers.IO).launch {
            checkInternetBackgroundRequest(requireContext())
        }

        // Initialize schedules ListView
        val schedulesAdapter: ArrayAdapter<Arrival> =
            SchedulesListAdapter(
                context,
                R.layout.adapter_view_layout,
                viewModel.schedulesArrayList
            )
        val schedulesListView = root.findViewById<ListView>(R.id.arrivalsListView)
        schedulesListView.isTransitionGroup
        schedulesListView.adapter = schedulesAdapter
        arrivalsProgressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            updateListViewBackgroundRequest(viewModel.fromSelection, viewModel.toSelection, schedulesListView, arrivalsProgressBar)
        }

        /* Set progressbar as visible while working */
        CoroutineScope(Dispatchers.IO).launch {
            checkSpecialBackgroundRequest(requireContext(), viewModel.fromSelection, viewModel.toSelection, specialProgressBar, root)
        }

        // Initialize from and to textViews
        val fromAutoCompleteTV =
            root.findViewById<AutoCompleteTextView>(R.id.fromTextView)
        val toAutoCompleteTV =
            root.findViewById<AutoCompleteTextView>(R.id.toTextView)
        val stationReverse =
            root.findViewById<Button>(R.id.reverseStationsButton)

        // Initialize array adapter for stations dropdown menu
        val stationsArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            viewModel.stationOptions
        )

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
                arrivalsProgressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    updateListViewBackgroundRequest(viewModel.fromSelection, viewModel.toSelection, schedulesListView, arrivalsProgressBar)
                }
                schedulesListView.adapter =
                    SchedulesListAdapter(
                        context,
                        R.layout.adapter_view_layout,
                        viewModel.schedulesArrayList
                    )
                /* Set progressbar as visible while working */
                specialProgressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    checkSpecialBackgroundRequest(requireContext(), viewModel.fromSelection, viewModel.toSelection, specialProgressBar, root)
                }
            }
        toAutoCompleteTV.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->

                // save from selection as variable
                viewModel.toSelection =
                    position // set destination station to index of selected array position
                // reload listview with new array and adapter and scroll to next train
                arrivalsProgressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    updateListViewBackgroundRequest(viewModel.fromSelection, viewModel.toSelection, schedulesListView, arrivalsProgressBar)
                }
                schedulesListView.adapter =
                    SchedulesListAdapter(
                        context,
                        R.layout.adapter_view_layout,
                        viewModel.schedulesArrayList
                    )
                /* Set progressbar as visible while working */
                specialProgressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    checkSpecialBackgroundRequest(requireContext(), viewModel.fromSelection, viewModel.toSelection, specialProgressBar, root)
                }
            }
        stationReverse.setOnClickListener {
            Toast.makeText(requireContext(), "HI REVERSE NOW", Toast.LENGTH_SHORT).show()
            var temp = viewModel.fromSelection
            viewModel.fromSelection = viewModel.toSelection
            viewModel.toSelection = temp

            arrivalsProgressBar.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                updateListViewBackgroundRequest(viewModel.fromSelection, viewModel.toSelection, schedulesListView, arrivalsProgressBar)
            }
            schedulesListView.adapter =
                SchedulesListAdapter(
                    context,
                    R.layout.adapter_view_layout,
                    viewModel.schedulesArrayList
                )
            /* Set progressbar as visible while working */
            specialProgressBar.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                checkSpecialBackgroundRequest(requireContext(), viewModel.fromSelection, viewModel.toSelection, specialProgressBar, root)
            }
            fromAutoCompleteTV.setText(viewModel.stationOptions[viewModel.fromSelection])
            toAutoCompleteTV.setText(viewModel.stationOptions[viewModel.toSelection])
        }

        return root
    }

    override fun onResume() {
        super.onResume()

        val root: View = binding.root

        // shared preferences
        val sharedPreferences = requireActivity().getSharedPreferences("com.davidp799.patcotoday_preferences", Context.MODE_PRIVATE)
        // 'from' and 'to' textViews
        val fromAutoCompleteTV =
            root.findViewById<AutoCompleteTextView>(R.id.fromTextView)
        val toAutoCompleteTV =
            root.findViewById<AutoCompleteTextView>(R.id.toTextView)
        // array adapter for stations dropdown menu
        val stationsArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            viewModel.stationOptions
        )
        // set default text for both textViews
        fromAutoCompleteTV.setText(sharedPreferences.getString("default_source", "Lindenwold"))
        toAutoCompleteTV.setText(sharedPreferences.getString("default_dest", "15-16th & Locust"))
        // connect textViews to stations options arrayAdapter
        fromAutoCompleteTV.setAdapter(stationsArrayAdapter)
        toAutoCompleteTV.setAdapter(stationsArrayAdapter)
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
        val serviceIdList = viewModel.schedules.getServiceID(viewModel.weekday)
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
            specialArrivals[kotlin.math.abs(routeId - 2)] // i made an oopsie with the routeid
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
            SchedulesListAdapter(
                context,
                R.layout.adapter_view_layout,
                viewModel.specialSchedulesArrayList
            )
        listView.adapter = specialArrayAdapter
        specialArrayAdapter.notifyDataSetChanged()
    }

    private fun scrollToNext(arrayList: ArrayList<Arrival>): Int {
        val date = Date()
        val timeFormat = SimpleDateFormat("h:mm aa", Locale.US)
        val timeFormatDate = timeFormat.format(date)
        var value = 0
        for (i in 0 until arrayList.size) {
            val thisArrival = arrayList[i]
            val v = thisArrival.arrivalTime.toString()
            try {
                if ( Objects.requireNonNull(timeFormat.parse(timeFormatDate)) == timeFormat.parse(v) ) { // curTime == varTime
                    break
                }
                if ( Objects.requireNonNull(timeFormat.parse(timeFormat.format(date))).before(timeFormat.parse(v)) ) { // curTime < varTime
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
    private suspend fun checkSpecialBackgroundRequest(context: Context, source: Int, destination: Int, progressIndicator: LinearProgressIndicator, view: View) {
        logThread("\n*** checkSpecialBackgroundRequest Active ***\n")
        // step 1: check internet status
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
                            setSpecialArrivalsOnMainThread(specialArrivals, progressIndicator, view, specialStatus)
                        }
                    }

                }
            }
        } else {
            configureBottomSheetOnMainThread(view, false, progressIndicator)
        }
    }
    private fun checkSpecial(): Boolean {
        return try {
            val doc = Jsoup.connect("http://www.ridepatco.org/schedules/schedules.asp").get()
            val getSpecial = GetSpecial(doc)
            viewModel.specialURLs.addAll(getSpecial.url)
            viewModel.specialTexts.addAll(getSpecial.text)
            viewModel.specialURLs.size > 0
        } catch (e: IOException) {
            e.printStackTrace()
            false
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
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null
        try {
            for (i in 0 until viewModel.specialURLs.size) {
                var filePath= viewModel.directory + "special/" + "special" + i
                try {
                    val url = URL(viewModel.specialURLs[i])
                    filePath += if (url.toString().contains(".jpg")) {
                        println(viewModel.specialText[i])
                        println("\n\n\nFOUND JPG FILE: $url\n\n\n")
                        ".jpg"
                    } else {
                        println("\n\n\nFOUND PDF FILE: $url\n\n\n")
                        ".pdf"
                    }
                    connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        Log.d("downloadPDF", "Server ResponseCode=" + connection.responseCode.toString() + " ResponseMessage=" + connection.responseMessage)
                    }
                    // download the file

                    input = connection.inputStream
                    Log.d("downloadPDF", "destinationFilePath=$filePath")
                    val newFile = File(filePath)
                    newFile.parentFile!!.mkdirs()
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
        return try {
            for (i in 0 until viewModel.specialURLs.size) {
                if (viewModel.specialURLs[i].contains(".jpg")) {
                    println("- Converting: special$i.jpg to pdf")
                    JpgToPdf(requireContext(),"special$i.jpg", "special$i.pdf")
                }
                val convertPDF = ConvertPDF( viewModel.directory + "special/", "special$i.pdf" )
                viewModel.runnableConvertedStrings.add(convertPDF.text)
            }
            viewModel.converted = true
            println("\n\n### CONVERTED PDF ###")
            for (i in 0 until viewModel.runnableConvertedStrings.size) {
                println(viewModel.runnableConvertedStrings[i])
            }
            println("###\n\n")
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun parseSpecial(): Boolean {
        return try {
            for (i in 0 until viewModel.runnableConvertedStrings.size) {
                val parsePDF = ParsePDF(viewModel.runnableConvertedStrings.get(i))
                viewModel.parsedArrivals.addAll(parsePDF.arrivalLines)
            }
            viewModel.specialWestBound.addAll(viewModel.parsedArrivals.get(0))
            viewModel.specialEastBound.addAll(viewModel.parsedArrivals.get(1))
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun getSpecialArrivalsBackground(source: Int, destination: Int): ArrayList<Arrival> {
        logThread("\n*** getSpecialArrivalsBackgroundRequest Active ***\n")
        return try {
            getSpecialSchedules(source, destination)
        } catch (e: Error) {
            e.printStackTrace()
            ArrayList()
        }
    }
    private suspend fun setSpecialArrivalsOnMainThread(input: ArrayList<Arrival>, progressIndicator: LinearProgressIndicator, view: View, specialStatus: Boolean) {
        withContext (Main) {
            setSpecialArrivals(input, progressIndicator, view, specialStatus)
        }
    }
    private fun setSpecialArrivals(input: ArrayList<Arrival>, progressIndicator: LinearProgressIndicator, view: View, specialStatus: Boolean){
        viewModel.specialSchedulesArrayList.clear()
        viewModel.specialSchedulesArrayList.addAll(input)
        configureBottomSheet(view, specialStatus)
        // Initialize special ListView
        val specialListView = view.findViewById<ListView>(R.id.specialArrivalsListView)
        val specialAbout = view.findViewById<TextView>(R.id.specialScheduleAbout)
        updateSpecialData(specialListView, specialAbout)
        progressIndicator.visibility = View.GONE
    }
    private suspend fun configureBottomSheetOnMainThread(view: View, specialStatus: Boolean, progressIndicator: LinearProgressIndicator) {
        withContext (Main) {
            configureBottomSheet(view, specialStatus)
            progressIndicator.visibility = View.GONE
        }
    }
    private fun configureBottomSheet(view: View, specialStatus: Boolean ) {
        /* Initialize Bottom Sheet and Special Loading Parameters */
        val mBottomSheetLayout = view.findViewById<LinearLayout>(R.id.bottom_sheet_layout)
        val sheetBehavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(mBottomSheetLayout)

        if (!specialStatus) {
            println("\n\n\n\n\n!!!!!!!! $specialStatus")
            sheetBehavior.peekHeight = 0
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED) // hide bottom sheet
        } else {
            println("\n\n\n\n\n!!!!!!!! $specialStatus")

            val headerArrowImage: ImageView =
                view.findViewById(R.id.bottom_sheet_arrow) // header arrow
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
            if (!viewModel.internet) {
                sheetBehavior.peekHeight = 0
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED) // hide bottom sheet
            } else {
                if (!viewModel.special) {
                    sheetBehavior.peekHeight = 0
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
                } else {
                    // initialize about and button for schedules
                    val specialAbout: TextView = view.findViewById(R.id.specialScheduleAbout)
                    val specialViewButton: Button = view.findViewById(R.id.specialScheduleViewButton)
                    specialViewButton.visibility = View.VISIBLE
                    val specialAboutValue = try {
                        viewModel.specialTexts[0].split(" | ")[1]
                    } catch (e: Exception) {
                        viewModel.specialTexts[0]
                    }
                    specialAbout.text = specialAboutValue
                    specialViewButton.setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.specialURLs[0])))
                    }
                    // debug
                    println("\n\n### PARSED PDF ###")
                    if (viewModel.specialSchedulesArrayList.size > 0 ) {
                        for (i in 0 until viewModel.specialSchedulesArrayList.size) {
                            println(viewModel.specialSchedulesArrayList[i])
                        }

                    } else {
                        println("EMPTY\nEMPTY\nEMPTY")
                    }
                    println("###\n\n")
                }
            }
        }
    }
    //
    //
    // Threads which update internet status and get special schedules in background
    private suspend fun checkInternetBackgroundRequest(context: Context) {
        logThread("\n*** checkInternetBackgroundRequest Active ***\n")
        val status = checkInternet(context)
        setInternetStatusOnMainThread(status)
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
    private suspend fun setInternetStatusOnMainThread(input: Boolean) {
        withContext (Main) {
            setInternetStatus(input)
        }
    }
    private fun setInternetStatus(input: Boolean){
        viewModel.internet = input
    }
    //
    //
    // Threads which update listView contents in background
    private suspend fun updateListViewBackgroundRequest(source: Int, destination: Int, listView: ListView, progressIndicator: LinearProgressIndicator) {
        logThread("\n*** updateListViewBackgroundRequest Active ***\n")
        val arrivals = getArrivalsBackground(source, destination)
        val value = scrollToNext(arrivals)
        setArrivalsOnMainThread(arrivals, listView, value, progressIndicator)
    }
    private fun getArrivalsBackground(source: Int, destination: Int): ArrayList<Arrival> {
        return try {
            getSchedules(source, destination)
        } catch (e: Error) {
            e.printStackTrace()
            ArrayList()
        }
    }
    private suspend fun setArrivalsOnMainThread(arrayList: ArrayList<Arrival>, listView: ListView, scrollValue: Int, progressIndicator: LinearProgressIndicator) {
        withContext (Main) {
            setArrivals(arrayList, listView, scrollValue, progressIndicator)
        }
    }
    private fun setArrivals(arrayList: ArrayList<Arrival>, listView: ListView, scrollValue: Int, arrivalsProgressBar: LinearProgressIndicator){
        viewModel.schedulesArrayList.clear()
        viewModel.schedulesArrayList.addAll(arrayList)
        val schedulesAdapter: ArrayAdapter<Arrival> =
            SchedulesListAdapter(
                context,
                R.layout.adapter_view_layout,
                viewModel.schedulesArrayList
            )
        listView.adapter = schedulesAdapter
        schedulesAdapter.notifyDataSetChanged()
        listView.smoothScrollToPositionFromTop(scrollValue, 0, 120)
        arrivalsProgressBar.visibility = View.GONE
    }
    // Logger function for background tasks (or other tasks...)
    private fun logThread(methodName: String){
        println("debug: ${methodName}: ${Thread.currentThread().name}")
    }
}
