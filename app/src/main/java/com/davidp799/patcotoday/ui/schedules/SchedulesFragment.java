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
import android.util.Log;
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.davidp799.patcotoday.utils.Arrival;
import com.davidp799.patcotoday.R;
import com.davidp799.patcotoday.utils.ConvertPDF;
import com.davidp799.patcotoday.utils.GetSpecial;
import com.davidp799.patcotoday.utils.ParsePDF;
import com.davidp799.patcotoday.utils.Schedules;
import com.davidp799.patcotoday.SchedulesListAdapter;
import com.davidp799.patcotoday.databinding.FragmentSchedulesBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.transition.MaterialFadeThrough;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SchedulesFragment extends Fragment {
    // TODO: Finish parsePDF() runnable; Create layout for special schedules sheet; Finish special schedules implementation...

    private FragmentSchedulesBinding binding;

    // Initialize Variables
    private int fromSelection, toSelection;
    private boolean internet, special, downloaded, converted, parsed;
    private static final String directory = "/data/data/com.davidp799.patcotoday/files/data/";
    private ArrayList<String> specialWestBound = new ArrayList<>();
    private ArrayList<String> specialEastBound = new ArrayList<>();
    private ArrayList<String> specialURL = new ArrayList<>();
    private ArrayList<String> specialText = new ArrayList<>();
    private ArrayList<Arrival> specialArrivals = new ArrayList<>();
    // bg lists
    private ArrayList<String> specialURLs = new ArrayList<>();
    private ArrayList<String> specialTexts = new ArrayList<>();
    private ArrayList<String> runnableConvertedStrings = new ArrayList<>();
    private ArrayList<ArrayList<String>> parsedArrivals = new ArrayList<>();
    //
    private Document doc;
    private final Schedules schedules = new Schedules();
    private static ConnectivityManager connectivityManager;
    private final int weekday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1; // weekday in java starts on sunday

    // Initialize Thread Handlers
    Handler downloadHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle downloadBundle = msg.getData();
            downloaded = downloadBundle.getBoolean("MSG_KEY");
        }
    };
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
            Bundle specialBundle = msg.getData();
            special = specialBundle.getBoolean("MSG_BOOLEAN");
            specialURL = specialBundle.getStringArrayList("MSG_URL");
            specialText = specialBundle.getStringArrayList("MSG_TEXT");
            if (!special) {
                Toast.makeText(getActivity(), "No Special Schedules Today", Toast.LENGTH_SHORT).show();
            }
        }
    };
    Handler convertHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle convertBundle = msg.getData();
            converted = convertBundle.getBoolean("MSG_BOOLEAN");
        }
    };
    Handler parseHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle parseBundle = msg.getData();
            specialWestBound = parseBundle.getStringArrayList("MSG_WB");
            specialEastBound = parseBundle.getStringArrayList("MSG_EB");
            parsed = parseBundle.getBoolean("MSG_KEY");
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

        /* Disable special schedules progress bar by default */
        LinearProgressIndicator specialProgressBar = root.findViewById(R.id.specialProgressIndicator);
        specialProgressBar.setVisibility(View.INVISIBLE);
        /* Initialize textview header for special schedules */
        TextView specialHeader = root.findViewById(R.id.specialScheduleHeader);

        // Background Activities - internet, special
        //backgroundTasks(directory);
        tasksThreadPool(directory);

        // Initialize arrayList for schedules and special schedules
        ListView schedulesListView = root.findViewById(R.id.arrivalsListView);
        ListView specialListView = root.findViewById(R.id.specialArrivalsListView);
        updateListView(schedulesListView, fromSelection, toSelection);
        updateSpecialData(specialListView, specialProgressBar, specialHeader, fromSelection, toSelection);

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
                updateListView(schedulesListView, position, toSelection);
                updateSpecialData(specialListView, specialProgressBar, specialHeader, fromSelection, toSelection);
            }
        });
        toAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override // save from selection as variable
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toSelection = position; // set destination station to index of selected array position
                // reload listview with new array and adapter and scroll to next train
                updateListView(schedulesListView, fromSelection, toSelection);
                updateSpecialData(specialListView, specialProgressBar, specialHeader, fromSelection, toSelection);
            }
        });

        /* Initialize Bottom Sheet and Special Loading Parameters */
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
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // hide bottom sheet
        } else {
            if (!special) {
                sheetBehavior.setPeekHeight(0);
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                /* obtain special info from saved array */
                System.out.println("im empty...");
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
            /* Re-initialize progress bar and title */
            LinearProgressIndicator specialProgressBar = getActivity().findViewById(R.id.specialProgressIndicator);
            TextView specialHeader = getActivity().findViewById(R.id.specialScheduleHeader);

            // reload listview with new array and adapter //
            ListView schedulesListView = getActivity().findViewById(R.id.arrivalsListView);
            ListView specialListView = getActivity().findViewById(R.id.specialArrivalsListView);
            updateListView(schedulesListView, fromSelection, toSelection);
            updateSpecialData(specialListView, specialProgressBar, specialHeader, fromSelection, toSelection);
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
    public ArrayList<Arrival> getSpecialSchedules(int source_id, int destination_id) {
        ArrayList<ArrayList<String>> specialArrivals = new ArrayList<>();
        specialArrivals.add(specialWestBound);
        specialArrivals.add(specialEastBound);

        int travelTime = schedules.getTravelTime(source_id, destination_id);
        int route_id = schedules.getRouteID(source_id, destination_id);
        // retrieve list of base data
        int position = 0;
        ArrayList<String> theArrivals = specialArrivals.get(Math.abs(route_id-2)); // i made an oopsie with the routeid
        ArrayList<String> temp = new ArrayList<>();
        try { // check for null
            for (int i=0; i < theArrivals.size(); i++) {
                if (position == 13) {
                    position = 0;
                }
                if (position == source_id) {
                    temp.add(theArrivals.get(i));
                }
                position += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedules.getFormatArrival(temp, travelTime);
    }
    /** Function used to reload schedules in provided listView.
     *  @param listView listView object for schedules
     *  @param source starting station
     *  @param destination arrival station */
    public void updateListView(ListView listView, int source, int destination) {

        ArrayList<Arrival> schedulesArrayList = getSchedules(source, destination);
        ArrayAdapter<Arrival> schedulesAdapter = new SchedulesListAdapter(getContext(), R.layout.adapter_view_layout, schedulesArrayList);
        listView.setAdapter(schedulesAdapter);
        schedulesAdapter.notifyDataSetChanged();
        // scroll to next train
        int value = scrollToNext(listView, schedulesArrayList);
        listView.smoothScrollToPositionFromTop(value, 0, 10);

    }
    /** Function used to reload special schedules in bottom sheet.
     *  @param listView listView object for schedules
     *  @param source starting station
     *  @param destination arrival station */
    public void updateSpecialData(ListView listView, LinearProgressIndicator progressIndicator, TextView header, int source, int destination) {
        /* Set progressbar as visible while working */
        progressIndicator.setVisibility(View.VISIBLE);
        /* Set special schedules header to special title */
        if (specialText.size() > 0) {
            header.setText(specialText.get(0));
        }
        /* Add formatted arrivals to special listview */
        ArrayList<Arrival> specialArrayList = getSpecialSchedules(source, destination);
        ArrayAdapter<Arrival> specialArrayAdapter = new SchedulesListAdapter(getContext(), R.layout.adapter_view_layout, specialArrayList);
        listView.setAdapter(specialArrayAdapter);
        specialArrayAdapter.notifyDataSetChanged();
        /* Set progressbar as invisible when finished */
        progressIndicator.setVisibility(View.INVISIBLE);
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
    /* Background Tasks using Runnable */
    public void backgroundTasks(String destinationFilePath) {
        /* Initialize common variables and assets */
        ArrayList<String> specialTexts = new ArrayList<>();
        ArrayList<String> specialURLs = new ArrayList<>();
        ArrayList<String> runnableConvertedStrings = new ArrayList<>();
        ArrayList<ArrayList<String>> parsedArrivals = new ArrayList<>();

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
                specialURLs.addAll(getSpecial.getUrl());
                specialTexts.addAll(getSpecial.getText());
                specialBundle.putBoolean("MSG_BOOLEAN", getSpecial.getStatus());
                specialBundle.putStringArrayList("MSG_URL", specialURLs);
                specialBundle.putStringArrayList("MSG_TEXT", specialTexts);
                specialMessage.setData(specialBundle);
                specialHandler.sendMessage(specialMessage);
            }
        };
        Runnable downloadRunnable = new Runnable() {
            final Message downloadMessage = downloadHandler.obtainMessage();
            final Bundle downloadBundle = new Bundle();

            @Override
            public void run() {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                for (int i=0; i<specialURLs.size(); i++) {
                    String filePath = directory + "/special/" + "special" + i + ".pdf";
                    System.out.println(specialTexts.get(i));
                    System.out.println(specialURLs.get(i));
                    try {
                        URL url = new URL(specialURLs.get(i));
                        connection = (HttpURLConnection) url.openConnection();
                        connection.connect();
                        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            Log.d("downloadPDF", "Server ResponseCode=" + connection.getResponseCode() + " ResponseMessage=" + connection.getResponseMessage());
                        }
                        // download the file
                        input = connection.getInputStream();
                        Log.d("downloadPDF", "destinationFilePath=" + filePath);
                        File newFile = new File(filePath);
                        newFile.getParentFile().mkdirs();
                        newFile.createNewFile();
                        output = new FileOutputStream(filePath);

                        byte[] data = new byte[4096];
                        int count;
                        while ((count = input.read(data)) != -1) {
                            output.write(data, 0, count);
                        }
                        downloaded = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    } finally {
                        try {
                            if (output != null) output.close();
                            if (input != null) input.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (connection != null) connection.disconnect();
                    }
                    File f = new File(destinationFilePath);
                    Log.d("downloadPDF", "f.getParentFile().getPath()=" + f.getParentFile().getPath());
                    Log.d("downloadPDF", "f.getName()=" + f.getName().replace(".pdf", ""));
                }
                downloadBundle.putBoolean("MSG_KEY", downloaded);
                downloadMessage.setData(downloadBundle);
                downloadHandler.sendMessage(downloadMessage);
            }
        };
        /* Runnable Thread for conversion of pdf data */
        Runnable convertRunnable = new Runnable() {
            final Message convertMessage = convertHandler.obtainMessage();
            final Bundle convertBundle = new Bundle();

            @Override
            public void run() {
                try {
                    for (int i = 0; i < specialURLs.size(); i++) {
                        System.out.println("- Converting: special" + i + ".pdf");
                        ConvertPDF convertPDF = new ConvertPDF(directory + "/special/", "special" + i + ".pdf");
                        System.out.println("- PDF" + i + " Characters = " + convertPDF.getText().length());
                        runnableConvertedStrings.add(convertPDF.getText());
                    }
                    converted = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                convertBundle.putStringArrayList("MSG_CONVERTED", runnableConvertedStrings); // send arraylist to handler
                convertBundle.putBoolean("MSG_BOOLEAN", converted); // send converted status to handler
                convertMessage.setData(convertBundle);
                convertHandler.sendMessage(convertMessage);
            }
        };
        Runnable parseRunnable = new Runnable() {
            final Message parseMessage = parseHandler.obtainMessage();
            final Bundle parseBundle = new Bundle();

            @Override
            public void run() {
                try {
                    for (int i = 0; i < runnableConvertedStrings.size(); i++) {
                        System.out.println("- Parsing Text" + i + ":");
                        System.out.println("-----------------------------------------------------------------------");
                        ParsePDF parsePDF = new ParsePDF(runnableConvertedStrings.get(i));
                        parsedArrivals.addAll(parsePDF.getArrivalLines());
                        for (int j = 0; j < parsedArrivals.size(); j++) {
                            if (j == 0) {
                                System.out.println("Westbound:");
                                System.out.println(parsedArrivals.get(j));
                            } else {
                                System.out.println("Eastbound:");
                                System.out.println(parsedArrivals.get(j));
                            }
                        }
                        System.out.println("-----------------------------------------------------------------------");
                    }
                    parsed = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    parsed = false;
                }
                if (parsedArrivals.size() > 0) {
                    parseBundle.putStringArrayList("MSG_WB", parsedArrivals.get(0));
                } if (parsedArrivals.size() > 1) {
                    parseBundle.putStringArrayList("MSG_EB", parsedArrivals.get(1));
                }
                parseBundle.putBoolean("MSG_KEY", parsed);
                parseMessage.setData(parseBundle);
                parseHandler.sendMessage(parseMessage);
            }
        };

        try {
            // First thread
            Thread t1_internet = new Thread(internetRunnable);
            t1_internet.start();
            t1_internet.join();
            // Second thread
            Thread t2_special = new Thread(specialRunnable);
            t2_special.start();
            t2_special.join();
            // Third thread
            Thread t3_download = new Thread(downloadRunnable);
            t3_download.start();
            t3_download.join();
            // Fourth thread
            Thread t4_convert = new Thread(convertRunnable);
            t4_convert.start();
            t4_convert.join();
            // Fifth thread
            Thread t5_parse = new Thread(parseRunnable);
            t5_parse.start();
            t5_parse.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /* Background Tasks using ExecutorService */
    public void checkInternetBg() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
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
            if (!internet) {
                handler.post(() -> Toast.makeText(getContext(), "No Internet Connection. Working Offline", Toast.LENGTH_LONG).show());
            }
        });
    }
    public void checkSpecialBg() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                doc = Jsoup.connect("http://www.ridepatco.org/schedules/schedules.asp").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            GetSpecial getSpecial = new GetSpecial(doc);
            specialURLs.addAll(getSpecial.getUrl());
            specialTexts.addAll(getSpecial.getText());
            if (!special) {
                handler.post(() -> Toast.makeText(getContext(), "No Special Schedules Today", Toast.LENGTH_SHORT).show());
            }
        });
    }
    public void downloadSpecialBg(String destinationFilePath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            for (int i = 0; i < specialURLs.size(); i++) {
                String filePath = directory + "/special/" + "special" + i + ".pdf";
                System.out.println(specialTexts.get(i));
                System.out.println(specialURLs.get(i));
                try {
                    URL url = new URL(specialURLs.get(i));
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.d("downloadPDF", "Server ResponseCode=" + connection.getResponseCode() + " ResponseMessage=" + connection.getResponseMessage());
                    }
                    // download the file
                    input = connection.getInputStream();
                    Log.d("downloadPDF", "destinationFilePath=" + filePath);
                    File newFile = new File(filePath);
                    newFile.getParentFile().mkdirs();
                    newFile.createNewFile();
                    output = new FileOutputStream(filePath);

                    byte[] data = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                    downloaded = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                } finally {
                    try {
                        if (output != null) output.close();
                        if (input != null) input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (connection != null) connection.disconnect();
                }
                File f = new File(destinationFilePath);
                Log.d("downloadPDF", "f.getParentFile().getPath()=" + f.getParentFile().getPath());
                Log.d("downloadPDF", "f.getName()=" + f.getName().replace(".pdf", ""));
            }
            handler.post(() -> Toast.makeText(getContext(), "downloadSpecialThread says: " + downloaded, Toast.LENGTH_SHORT).show());
        });
    }
    public void convertSpecialBg() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                for (int i = 0; i < specialURLs.size(); i++) {
                    System.out.println("- Converting: special" + i + ".pdf");
                    ConvertPDF convertPDF = new ConvertPDF(directory + "/special/", "special" + i + ".pdf");
                    System.out.println("- PDF" + i + " Characters = " + convertPDF.getText().length());
                    runnableConvertedStrings.add(convertPDF.getText());
                }
                converted = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(() -> Toast.makeText(getContext(), "convertSpecialThread says: " + converted, Toast.LENGTH_SHORT).show());
        });
    }
    public void parseSpecialBg() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                for (int i = 0; i < runnableConvertedStrings.size(); i++) {
                    System.out.println("- Parsing Text" + i + ":");
                    System.out.println("-----------------------------------------------------------------------");
                    ParsePDF parsePDF = new ParsePDF(runnableConvertedStrings.get(i));
                    parsedArrivals.addAll(parsePDF.getArrivalLines());
                    for (int j = 0; j < parsedArrivals.size(); j++) {
                        if (j == 0) {
                            System.out.println("Westbound:");
                            System.out.println(parsedArrivals.get(j));
                        } else {
                            System.out.println("Eastbound:");
                            System.out.println(parsedArrivals.get(j));
                        }
                    }
                    System.out.println("-----------------------------------------------------------------------");
                }
                parsed = true;
            } catch (Exception e) {
                e.printStackTrace();
                parsed = false;
            }
            specialWestBound.addAll(parsedArrivals.get(0));
            specialEastBound.addAll(parsedArrivals.get(1));
            handler.post(() -> Toast.makeText(getContext(), "parseSpecialThread says: " + parsed, Toast.LENGTH_SHORT).show());
        });
    }
    public void tasksThreadPool(String destinationFilePath) {
        final int DEFAULT_THREAD_POOL_SIZE = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

        executorService.execute(this::checkInternetBg);
        if (internet) {
            executorService.execute(this::checkSpecialBg);
            if (special) {
                executorService.execute(() -> {
                    downloadSpecialBg(destinationFilePath);
                });
                if (downloaded) {
                    executorService.execute(this::convertSpecialBg);
                    if (converted) {
                        executorService.execute(this::parseSpecialBg);
                    }
                }
            }
        }
    }
}