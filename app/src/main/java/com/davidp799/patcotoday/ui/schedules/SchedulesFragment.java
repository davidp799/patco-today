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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.davidp799.patcotoday.R;
import com.davidp799.patcotoday.Schedules;
import com.davidp799.patcotoday.databinding.FragmentSchedulesBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.transition.MaterialFadeThrough;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
        special = true; // DEBUG

        // Initialize arrayList for schedules
        ArrayList<String> schedulesArrayList = getSchedules(fromSelection, toSelection);
        ListView schedulesListView = root.findViewById(R.id.arrivalsListView);
        ArrayAdapter<String> schedulesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, schedulesArrayList);
        schedulesListView.setAdapter(schedulesAdapter);
        schedulesListView.setTransitionGroup(true);

        // Scroll to Next Train in Schedules ListView
        Date date = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa", Locale.US) ;
        timeFormat.format(date);
        int value = 0;
        for (int i = 0; i < schedulesListView.getCount(); i++) { // get position value for next train
            String v = String.valueOf(schedulesArrayList.get(i));
            try {
                if ((timeFormat.parse(timeFormat.format(date)).equals(timeFormat.parse(v)))) { // curTime == varTime
                    break;
                } if (timeFormat.parse(timeFormat.format(date)).before(timeFormat.parse(v))) { // curTime < varTime
                    break;
                } value = i+1;
            } catch (ParseException e) { e.printStackTrace(); }
        }
        schedulesListView.smoothScrollToPositionFromTop(value,0,10);
        schedulesListView.setSelection(value);
        schedulesAdapter.notifyDataSetChanged();

        // Initialize Exposed Dropdown Menus
        ArrayAdapter<String> fromArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, stationOptionsList); // create array adapter
        AutoCompleteTextView fromAutoCompleteTV = root.findViewById(R.id.fromTextView); // get reference to autocomplete text view
        fromAutoCompleteTV.setText(sharedPreferences.getString("default_source", "Lindenwold"));
        fromAutoCompleteTV.setAdapter(fromArrayAdapter); // populate menu for source TV
        fromAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromSelection = position; // save selection for source station

                // reload listview with new array and adapter //
                ArrayList<String> schedulesArrayList = getSchedules(fromSelection, toSelection);
                ArrayAdapter<String> schedulesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, schedulesArrayList);
                schedulesListView.setAdapter(schedulesAdapter);
                schedulesAdapter.notifyDataSetChanged();
                // scroll to next train //
                Date date = new Date() ;
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa", Locale.US) ;
                String timeFormatDate = timeFormat.format(date);
                int value = 0;
                for (int i = 0; i < schedulesListView.getCount(); i++) {
                    String v = String.valueOf(schedulesArrayList.get(i));
                    try {
                        if ((Objects.requireNonNull(timeFormat.parse(timeFormatDate)).equals(timeFormat.parse(v)))) { // curTime == varTime
                            break;
                        } if (Objects.requireNonNull(timeFormat.parse(timeFormat.format(date))).before(timeFormat.parse(v))) { // curTime < varTime
                            break;
                        } value = i+1;
                    } catch (ParseException e) { e.printStackTrace(); }
                }
                schedulesAdapter.notifyDataSetChanged();
                schedulesListView.smoothScrollToPositionFromTop(value, 0, 10);
            }
        });

        // to exposed-dropdown-menu
        ArrayAdapter<String> toArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, stationOptionsList); // create array adapter and pass parameters (context, dropdown layout, array)
        AutoCompleteTextView toAutoCompleteTV= root.findViewById(R.id.toTextView); // get reference to autocomplete text view
        toAutoCompleteTV.setText(sharedPreferences.getString("default_dest", "15-16th & Locust" ));
        toAutoCompleteTV.setAdapter(toArrayAdapter); // set adapter to autocomplete tv to arrayAdapter
        toAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override // save from selection as variable
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toSelection = position; // account for positioning in array
                // reload listview with new array and adapter //
                ArrayList<String> schedulesArrayList = getSchedules(fromSelection, toSelection);
                ArrayAdapter<String> schedulesAdapter = new ArrayAdapter<>(
                        getActivity(), android.R.layout.simple_list_item_1, schedulesArrayList);
                schedulesListView.setAdapter(schedulesAdapter);
                schedulesAdapter.notifyDataSetChanged();
                // scroll to next train //                Date date = new Date() ;
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa", Locale.US) ;
                timeFormat.format(date);
                int value = 0;
                for (int i = 0; i < schedulesListView.getCount(); i++) {
                    String v = String.valueOf(schedulesArrayList.get(i));
                    try {
                        if ((timeFormat.parse(timeFormat.format(date)).equals(timeFormat.parse(v)))) { // curTime == varTime
                            break;
                        } if (timeFormat.parse(timeFormat.format(date)).before(timeFormat.parse(v))) { // curTime < varTime
                            break;
                        } value = i+1;
                    } catch (ParseException e) { e.printStackTrace(); }
                }
                schedulesAdapter.notifyDataSetChanged();
                schedulesListView.smoothScrollToPositionFromTop(value, 0, 10);

                if (value > 0) {
                    Toast.makeText(getActivity(),
                            String.format("Next train arrives at %s",
                                    schedulesListView.getItemAtPosition(value).toString()),
                            Toast.LENGTH_SHORT).show(); //debug
                }
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
            // Initialize web view for bottom sheet
            WebView webView = (WebView) root.findViewById(R.id.webview);
            WebSettings webSettings = webView.getSettings();
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            webView.loadUrl("http://www.ridepatco.org/schedules/schedules.asp");
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
            ArrayList<String> schedulesArrayList = getSchedules(fromSelection, toSelection);
            ArrayAdapter<String> schedulesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, schedulesArrayList);
            schedulesListView.setAdapter(schedulesAdapter);
            schedulesAdapter.notifyDataSetChanged();
            // scroll to next train //
            Date currentDateTime = new Date() ;
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa", Locale.US) ;
            timeFormat.format(currentDateTime);

            int value = 0;
            for (int i = 0; i < schedulesListView.getCount(); i++) {
                String v = String.valueOf(schedulesArrayList.get(i));
                try {
                    if ((timeFormat.parse(timeFormat.format(currentDateTime)).equals(timeFormat.parse(v)))) { // curTime == varTime
                        break;
                    } if (timeFormat.parse(timeFormat.format(currentDateTime)).before(timeFormat.parse(v))) { // curTime < varTime
                        break;
                    } value = i+1;
                } catch (ParseException e) { e.printStackTrace(); }
            }
            schedulesAdapter.notifyDataSetChanged();
            schedulesListView.smoothScrollToPositionFromTop(value, 0, 10);
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
    public ArrayList<String> getSchedules(int source_id, int destination_id) {
        // Initialize travel duration and route_id for current trip
        int travelTime = schedules.getTravelTime(source_id, destination_id);
        int route_id = schedules.getRouteID(source_id, destination_id);
        // Retrieve lists of base data
        ArrayList<Integer> service_idList = schedules.getServiceID(weekday);
        ArrayList<String> trip_idList = schedules.getTripID(route_id, service_idList);
        // Retrieve unformatted list of arrival times
        ArrayList<String> schedulesList = schedules.getSchedulesList(trip_idList, fromSelection);
        // Return formatted list of arrival times for current trip
        return schedules.getFormatSchedulesList(schedulesList, travelTime);
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
    } public void checkSpecial() {
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
                // initialize elements
                Calendar cal = Calendar.getInstance();
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                Element table = doc.body().getElementsByTag("table").first();
                Element tbody = table.getElementsByTag("tbody").first();
                // check for special schedule
                for (Element tr : tbody.getElementsByTag("tr")) {
                    for (Element td : tr.getElementsByTag("td")) {
                        for (Element p : td.getElementsByTag("p")) {
                            if (p.text().contains(", " + (month + 1) + "/" + day)) {
                                special = true;
                            } else if (p.text().contains("(" + (month + 1) + "/" + day + ")")) {
                                special = true;
                            }
                        }
                    }
                }
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