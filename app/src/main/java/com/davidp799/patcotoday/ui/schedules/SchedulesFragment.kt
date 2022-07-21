package com.davidp799.patcotoday.ui.schedules

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import com.davidp799.patcotoday.MainViewModel
import com.davidp799.patcotoday.R
import com.davidp799.patcotoday.SchedulesListAdapter
import com.davidp799.patcotoday.databinding.FragmentSchedulesBinding
import com.davidp799.patcotoday.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.*
import java.lang.Exception
import java.lang.Math.abs
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.collections.ArrayList

class SchedulesFragment : Fragment() {

    private var _binding: FragmentSchedulesBinding? = null

    // Station Data
    private var fromSelection = 0
    private var toSelection = 0
    private val weekday =
        Calendar.getInstance()[Calendar.DAY_OF_WEEK] - 1 // weekday in java starts on sunday

    // Job Management
    private var internet = false
    private val special = false
    private var downloaded = false
    private var converted = false
    private var parsed = false
    private var doc: Document? = null
    private val schedules = Schedules()

    // Background Threading
    private val specialEastBound = ArrayList<String>()
    private val specialWestBound = ArrayList<String>()
    private val specialText = ArrayList<String>()
    private val specialURLs = ArrayList<String>()
    private val specialTexts = ArrayList<String>()
    private val runnableConvertedStrings = ArrayList<String>()
    private val parsedArrivals = ArrayList<ArrayList<String>>()
    private val schedulesArrayList = ArrayList<Arrival>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by ViewModelLazy(
        MainViewModel::class, {viewModelStore }, { defaultViewModelProviderFactory }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val schedulesViewModel =
            ViewModelProvider(this).get(SchedulesViewModel::class.java)
        _binding = FragmentSchedulesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        enterTransition = MaterialFadeThrough()

        // implement reverse menu button
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_reverse, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // IMPLEMENT ACTIONS HERE
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // shared preferences
        val sharedPreferences = requireActivity().getSharedPreferences(
            "com.davidp799.patcotoday_preferences",
            Context.MODE_PRIVATE
        )

        // Initialize stations options list data
        val stationOptionsList = listOf(*resources.getStringArray(R.array.stations_list))
        fromSelection =
            stationOptionsList.indexOf(sharedPreferences.getString("default_source", "Lindenwold"))
        toSelection = stationOptionsList.indexOf(
            sharedPreferences.getString(
                "default_dest",
                "15-16th & Locust"
            )
        )

        // Manage Special Schedules Progress Bar
        val specialProgressBar: LinearProgressIndicator =
            root.findViewById(R.id.specialProgressIndicator)
        // Special Schedules Title TextView (header)
        val specialHeader = root.findViewById<TextView>(R.id.specialScheduleHeader)

        // Background Activities - internet, special, etc...
        //tasksThreadPool(directory)

        // Initialize schedules ListView
        val schedulesListView = root.findViewById<ListView>(R.id.arrivalsListView)
        CoroutineScope(Dispatchers.IO).launch {
            backgroundTasksRequest(fromSelection, toSelection)
        }
        val schedulesAdapter: ArrayAdapter<Arrival> =
            SchedulesListAdapter(context, R.layout.adapter_view_layout, schedulesArrayList)
        schedulesListView.adapter = schedulesAdapter

        val value = scrollToNext(schedulesListView, schedulesArrayList)
        schedulesListView.smoothScrollToPositionFromTop(value, 0, 10)

        // Initialize special ListView
        val specialListView = root.findViewById<ListView>(R.id.specialArrivalsListView)
        updateSpecialData(
            specialListView,
            specialProgressBar,
            specialHeader,
            fromSelection,
            toSelection
        )

        // Initialize array adapter for stations dropdown menu
        val stationsArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            stationOptionsList
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
                fromSelection = position // set source station to index of selected array position
                // reload listview and scroll to next train
                CoroutineScope(Dispatchers.IO).launch {
                    backgroundTasksRequest(fromSelection, toSelection)
                }
                val schedulesAdapter: ArrayAdapter<Arrival> =
                    SchedulesListAdapter(context, R.layout.adapter_view_layout, schedulesArrayList)
                schedulesListView.adapter = schedulesAdapter

                updateSpecialData(
                    specialListView,
                    specialProgressBar,
                    specialHeader,
                    fromSelection,
                    toSelection
                )
            }
        toAutoCompleteTV.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->

                // save from selection as variable
                toSelection =
                    position // set destination station to index of selected array position
                // reload listview with new array and adapter and scroll to next train
                CoroutineScope(Dispatchers.IO).launch {
                    backgroundTasksRequest(fromSelection, toSelection)
                }
                val schedulesAdapter: ArrayAdapter<Arrival> =
                    SchedulesListAdapter(context, R.layout.adapter_view_layout, schedulesArrayList)
                schedulesListView.adapter = schedulesAdapter

                updateSpecialData(
                    specialListView,
                    specialProgressBar,
                    specialHeader,
                    fromSelection,
                    toSelection
                )
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
        if (!internet) {
            //Toast.makeText(activity, "No Connection: Working Offline", Toast.LENGTH_SHORT).show()
            sheetBehavior.peekHeight = 0
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED) // hide bottom sheet
        } else {
            //Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show()
            if (!special) {
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

    /** Function which uses executorService to update ListView content in the background.  */
/*    private fun updateListViewBg(listView: ListView, source: Int, destination: Int) {
        Handler().post {
            schedulesArrayList.clear()
            schedulesArrayList.addAll(getSchedules(source, destination))
            val schedulesAdapter: ArrayAdapter<Arrival> =
                SchedulesListAdapter(context, R.layout.adapter_view_layout, schedulesArrayList)
            requireActivity().runOnUiThread { listView.adapter = schedulesAdapter }
        }
    }*/

    /** Function used to retrieve arrival times from Schedules class.  */
    private fun getSchedules(source_id: Int, destination_id: Int): ArrayList<Arrival> {
        // Initialize travel duration and routeId for current trip
        val travelTime = schedules.getTravelTime(source_id, destination_id)
        val routeId = schedules.getRouteID(source_id, destination_id)
        // Retrieve lists of base data
        val serviceIdList = schedules.getServiceID(weekday)
        val tripIdList = schedules.getTripID(routeId, serviceIdList)
        // Retrieve unformatted list of arrival times
        val schedulesList = schedules.getSchedulesList(tripIdList, fromSelection)
        // Return formatted list of arrival times for current trip
        return schedules.getFormatArrival(schedulesList, travelTime)
    }

    private fun getSpecialSchedules(source_id: Int, destination_id: Int): ArrayList<Arrival> {
        val specialArrivals = ArrayList<ArrayList<String>>()
        specialArrivals.add(specialWestBound)
        specialArrivals.add(specialEastBound)
        val travelTime = schedules.getTravelTime(source_id, destination_id)
        val routeId = schedules.getRouteID(source_id, destination_id)
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
        return schedules.getFormatArrival(temp, travelTime)
    }

    /** Function used to reload special schedules in bottom sheet.
     * @param listView listView object for schedules
     * @param source starting station
     * @param destination arrival station
     */
    private fun updateSpecialData(
        listView: ListView,
        progressIndicator: LinearProgressIndicator,
        header: TextView,
        source: Int,
        destination: Int
    ) {
        /* Set progressbar as visible while working */
        progressIndicator.visibility = View.VISIBLE
        /* Set special schedules header to special title */
        if (specialText.size > 0) { header.text = specialText[0] }
        /* Add formatted arrivals to special listview */
        val specialArrayList = getSpecialSchedules(source, destination)
        val specialArrayAdapter =
            SchedulesListAdapter(context, R.layout.adapter_view_layout, specialArrayList)
        listView.adapter = specialArrayAdapter
        specialArrayAdapter.notifyDataSetChanged()
        /* Set progressbar as invisible when finished */
        progressIndicator.visibility = View.INVISIBLE
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





    /* Background Tasks using Kotlin Coroutine: internet, update, download, extract */
    private fun logThread(methodName: String){
        println("debug: ${methodName}: ${Thread.currentThread().name}")
    }

    private fun setInternetStatus(input: Boolean){
        mainViewModel.internet = input
    }
    private suspend fun setInternetStatusOnMainThread(input: Boolean) {
        withContext (Main) {
            setInternetStatus(input)
            if (!input) {
                Toast.makeText(requireContext(), "No internet connection. Working offline", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Internet Connected", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setArrivals(input: ArrayList<Arrival>){
        schedulesArrayList.clear()
        schedulesArrayList.addAll(input)
    }
    private suspend fun setArrivalsOnMainThread(input: ArrayList<Arrival>) {
        withContext (Main) {
            setArrivals(input)
        }
    }

    private suspend fun backgroundTasksRequest(source: Int, destination: Int) {
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

    private fun checkInternet(context: Context): Boolean {
        logThread("checkInternetBackgroundActive")
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


}