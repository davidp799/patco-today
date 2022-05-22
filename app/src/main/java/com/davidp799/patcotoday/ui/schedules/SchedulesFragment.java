package com.davidp799.patcotoday.ui.schedules;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.davidp799.patcotoday.utilities.Arrival;
import com.davidp799.patcotoday.R;
import com.davidp799.patcotoday.utilities.GetSpecial;
import com.davidp799.patcotoday.utilities.Schedules;
import com.davidp799.patcotoday.SchedulesListAdapter;
import com.davidp799.patcotoday.databinding.FragmentSchedulesBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.transition.MaterialFadeThrough;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class SchedulesFragment extends Fragment {
    private FragmentSchedulesBinding binding;

    // Initialize Variables
    private int fromSelection, toSelection;
    private boolean internet, special;
    private Document doc;
    private final Schedules schedules = new Schedules();
    private static ConnectivityManager connectivityManager;
    private final int weekday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1; // weekday in java starts on sunday

    // Initialize Thread Handlers
    Handler internetHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            internet = msg.getData().getBoolean("MSG_KEY");
        }
    };
    Handler specialHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            special = msg.getData().getBoolean("MSG_KEY");
            Toast.makeText(getActivity(), String.format("Special: %s", special), Toast.LENGTH_SHORT).show();
        }
    };

    /* Initialize onCreate */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSchedulesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // User Interface
        setHasOptionsMenu(true);
        setEnterTransition(new MaterialFadeThrough());
        // Shared Preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("com.davidp799.patcotoday_preferences", MODE_PRIVATE);
        // Initialize Variables
        ArrayList<String> stationOptionsList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.stations_list)));
        fromSelection = stationOptionsList.indexOf(sharedPreferences.getString("default_source", "Lindenwold"));
        toSelection = stationOptionsList.indexOf(sharedPreferences.getString("default_dest", "15-16th & Locust"));

        // Background Activities - network, special
        checkInternet();
        // checkSpecial();
        special = true;

        // Initialize arrayList for schedules
        ListView schedulesListView = root.findViewById(R.id.arrivalsListView);
        updateListView(root, schedulesListView, fromSelection, toSelection);

        // Initialize array adapter for stations dropdown menu
        ArrayAdapter<String> stationsArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, stationOptionsList); // create array adapter
        // Initialize from and to textViews
        AutoCompleteTextView fromAutoCompleteTV = root.findViewById(R.id.fromTextView); // get reference to autocomplete text view
        AutoCompleteTextView toAutoCompleteTV= root.findViewById(R.id.toTextView); // get reference to autocomplete text view
        // set default text for both textViews
        fromAutoCompleteTV.setText(sharedPreferences.getString("default_source", "Lindenwold"));
        toAutoCompleteTV.setText(sharedPreferences.getString("default_dest", "15-16th & Locust" ));

        // connect textViews to stations options arrayAdapter
        fromAutoCompleteTV.setAdapter(stationsArrayAdapter);
        toAutoCompleteTV.setAdapter(stationsArrayAdapter);

        // Listen for station selections: from and to respectively
        fromAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromSelection = position; // set source station to index of selected array position
                // reload listview and scroll to next train
                updateListView(root, schedulesListView, position, toSelection);
            }
        });
        toAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override // save from selection as variable
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toSelection = position; // set destination station to index of selected array position
                // reload listview with new array and adapter and scroll to next train
                updateListView(root, schedulesListView, fromSelection, toSelection);
            }
        });

        // Initialize Bottom Sheet Parameters //
        LinearLayout mBottomSheetLayout = root.findViewById(R.id.bottom_sheet_layout);
        BottomSheetBehavior<LinearLayout> sheetBehavior;
        ImageView header_Arrow_Image; // header arrow
        sheetBehavior = BottomSheetBehavior.from(mBottomSheetLayout);
        header_Arrow_Image = root.findViewById(R.id.bottom_sheet_arrow);
        // Header arrow implementation for bottom sheet
        header_Arrow_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        // Implement bottom sheet call
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                header_Arrow_Image.setRotation(slideOffset * 180);
            }
        });
        // Check for internet connection
        if (!internet) {
            Toast.makeText(getActivity(), "No Connection: Working Offline", Toast.LENGTH_SHORT).show();
            sheetBehavior.setPeekHeight(0);
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            /* ENTER SPECIAL SCHEDULE INFO HERE */
            if (!special) {
                sheetBehavior.setPeekHeight(0);
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
        return root;
    }
    /* Initialize reverse trip button in top app bar */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.reversemenu, menu);
    }
    /* Reverse and reload data for listView and textViews on reverse button click */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_reverse){
            ArrayList<String> stationOptionsList = new ArrayList<>(
                    Arrays.asList(getResources().getStringArray(R.array.stations_list)));
            int temp = fromSelection;
            fromSelection = toSelection;
            toSelection = temp;

            // change textview values
            ArrayAdapter<String> itemsArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, stationOptionsList);
            AutoCompleteTextView fromAutoCompleteTV= getActivity().findViewById(R.id.fromTextView); // get reference to autocomplete text view
            fromAutoCompleteTV.setText(stationOptionsList.get(fromSelection));
            fromAutoCompleteTV.setAdapter(itemsArrayAdapter); // set adapter to autocomplete tv to arrayAdapter
            AutoCompleteTextView toAutoCompleteTV= getActivity().findViewById(R.id.toTextView); // get reference to autocomplete text view
            toAutoCompleteTV.setText(stationOptionsList.get(toSelection));
            toAutoCompleteTV.setAdapter(itemsArrayAdapter); // set adapter to autocomplete tv to arrayAdapter

            // reload listview with new array and adapter //
            ListView schedulesListView = getActivity().findViewById(R.id.arrivalsListView);
            updateListView(getView(), schedulesListView, fromSelection, toSelection);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /** Function used to retrieve arrival times from Schedules class.
     *  @param source_id starting station
     *  @param destination_id arrival station
     *  @return ArrayList of strings */
    public ArrayList<Arrival> getSchedules(int source_id, int destination_id) {
        // Initialize travel duration and route_id for current trip
        int travelTime = schedules.getTravelTime(source_id, destination_id);
        int route_id = schedules.getRouteID(source_id, destination_id);
        // Retrieve lists of base data
        ArrayList<Integer> service_idList = schedules.getServiceID(weekday);
        ArrayList<String> trip_idList = schedules.getTripID(route_id, service_idList);
        // Retrieve unformatted list of arrival times
        ArrayList<String> schedulesList = schedules.getSchedulesList(trip_idList, fromSelection);
        // Return formatted list of arrival times for current trip
        return schedules.getFormatArrival(schedulesList, travelTime);
    }

    /** Function used to reload schedules in provided listView.
     *  @param listView listView object for schedules
     *  @param source starting station
     *  @param destination arrival station */
    public void updateListView(View view, ListView listView, int source, int destination) {

        ArrayList<Arrival> schedulesArrayList = getSchedules(source, destination);
        ArrayAdapter<Arrival> schedulesAdapter = new SchedulesListAdapter(getContext(), R.layout.adapter_view_layout, schedulesArrayList);
        listView.setAdapter(schedulesAdapter);
        schedulesAdapter.notifyDataSetChanged();
        // scroll to next train
        int value = scrollToNext(listView, schedulesArrayList);
        listView.smoothScrollToPositionFromTop(value, 0, 10);

    }

    public int scrollToNext(@NonNull ListView listView, ArrayList<Arrival> arrayList) {
        Date date = new Date() ;
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa", Locale.US) ;
        String timeFormatDate = timeFormat.format(date);
        int value = 0;
        for (int i = 0; i < listView.getCount(); i++) {
            Arrival thisArrival = arrayList.get(i);
            String v = String.valueOf(thisArrival.getArrivalTime());
            try {
                if ((Objects.requireNonNull(timeFormat.parse(timeFormatDate)).equals(timeFormat.parse(v)))) { // curTime == varTime
                    break;
                } if (Objects.requireNonNull(timeFormat.parse(timeFormat.format(date))).before(timeFormat.parse(v))) { // curTime < varTime
                    break;
                } value = i+1;
            } catch (ParseException e) { e.printStackTrace(); }
        } return value;
    }

    /* Background Threads - checkInternet, checkSpecial */
    public void checkInternet() {
        Runnable internetRunnable = new Runnable() {
            final Message internetMessage = internetHandler.obtainMessage();
            final Bundle internetBundle = new Bundle();
            @Override
            public void run() {
                try {
                    connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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
    public void checkSpecial() {
        Runnable specialRunnable = new Runnable() {
            final Message specialMessage = specialHandler.obtainMessage();
            final Bundle specialBundle = new Bundle();
            @Override
            public void run() {
                try {
                    doc = Jsoup.connect("http://www.ridepatco.org/schedules/schedules.asp").get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // get special status
                GetSpecial getSpecial = new GetSpecial(doc);
                special = getSpecial.getStatus();

                specialBundle.putBoolean("MSG_KEY", special);
                specialMessage.setData(specialBundle);
                specialHandler.sendMessage(specialMessage);
            }
        };
        Thread specialBgThread = new Thread(specialRunnable);
        try {
            specialBgThread.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        specialBgThread.start();
    }
}