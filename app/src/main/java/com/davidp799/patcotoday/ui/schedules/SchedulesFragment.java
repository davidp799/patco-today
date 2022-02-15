package com.davidp799.patcotoday.ui.schedules;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.davidp799.patcotoday.MainActivity;
import com.davidp799.patcotoday.R;
import com.davidp799.patcotoday.databinding.FragmentSchedulesBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SchedulesFragment extends Fragment {

    private FragmentSchedulesBinding binding;
    private boolean special;

    // progress bar
    private Handler handler = new Handler();

    // global variables for arrivals and from and to selections
    int fromSelection, toSelection;
    ArrayList<String> arrivals = new ArrayList<String>();
    List<PyObject> travelTimes;

    Handler bgHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle bgBundle = msg.getData();
            boolean myMessage = bgBundle.getBoolean("MSG_KEY");

            special = msg.getData().getBoolean("MSG_KEY");
            if (special) {
                Toast.makeText(getActivity(), String.format("special: %s", special), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // initial parameters
        binding = FragmentSchedulesBinding.inflate(inflater, container, false);
        initPython(); // initialize python3
        View root = binding.getRoot();
        setHasOptionsMenu(true);

        /// . . . List View START . . . ///
        ListView arrivalTimes = (ListView) root.findViewById(R.id.arrivalTimes); // init arrivals listview
        ArrayAdapter<String> arrivalsGeneralAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                arrivals
        );
        arrivalTimes.setAdapter(arrivalsGeneralAdapter);

        background(fromSelection, toSelection); // populate list
        // populate list from pyObject listSchedules
        for (int i=0; i<travelTimes.size(); i++) {
            String res = travelTimes.get(i).toJava(String.class);
            arrivals.add(res);
        }

        // scroll to next train PARTIALLY FUNCTIONAL
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm aa") ;
        dateFormat.format(date);
        System.out.println(dateFormat.format(date));

        int value = 0;
        for (int i = 0; i < arrivalTimes.getCount(); i++) {
            String v = String.valueOf(travelTimes.get(i));
            try {
                if ((dateFormat.parse(dateFormat.format(date)).equals(dateFormat.parse(v)))) { // curTime == varTime
                    break;
                } if (dateFormat.parse(dateFormat.format(date)).before(dateFormat.parse(v))) { // curTime < varTime
                    break;
                } value = i+1;
            } catch (ParseException e) { e.printStackTrace(); }
        }
        arrivalTimes.smoothScrollToPositionFromTop(value,0,10);
        arrivalTimes.setSelection(value);
        Toast.makeText(getActivity(), String.format("Next train arrives at %s", arrivalTimes.getItemAtPosition(value).toString()), Toast.LENGTH_LONG).show(); //debug
        /// . . . List View END . . . ///




        // from exposed-dropdown-menu
        ArrayList<String> stationsList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.stations_list)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, stationsList); // create array adapter and pass parameters (context, dropdown layout, array)
        AutoCompleteTextView autocompleteTV = root.findViewById(R.id.fromTextView); // get reference to autocomplete text view
        autocompleteTV.setAdapter(arrayAdapter); // set adapter to autocomplete tv to arrayAdapter
        autocompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override // save from selection as variable
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromSelection = position;
                arrivalsGeneralAdapter.notifyDataSetChanged();
            }
        });
        // to exposed-dropdown-menu
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, stationsList); // create array adapter and pass parameters (context, dropdown layout, array)
        AutoCompleteTextView autocompleteTV2= root.findViewById(R.id.toTextView); // get reference to autocomplete text view
        autocompleteTV2.setAdapter(arrayAdapter2); // set adapter to autocomplete tv to arrayAdapter
        autocompleteTV2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override // save to selection as variable
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toSelection = position;
                /// . . . List View START . . . ///
                ListView arrivalTimes = (ListView) root.findViewById(R.id.arrivalTimes); // init arrivals listview
                ArrayAdapter<String> arrivalsGeneralAdapter = new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_list_item_1,
                        arrivals
                );
                arrivalTimes.setAdapter(arrivalsGeneralAdapter);

                background(fromSelection, toSelection);
                // populate list from pyObject listSchedules
                for (int i=0; i<travelTimes.size(); i++) {
                    String res = travelTimes.get(i).toJava(String.class);
                    arrivals.add(res);
                }
                Toast.makeText(getActivity(), String.format("%s", arrivals.size()), Toast.LENGTH_LONG).show(); //debug

                // scroll to next train PARTIALLY FUNCTIONAL
                Date date = new Date() ;
                SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm aa") ;
                dateFormat.format(date);
                System.out.println(dateFormat.format(date));

                int value = 0;
                for (int i = 0; i < arrivalTimes.getCount(); i++) {
                    String v = String.valueOf(travelTimes.get(i));
                    try {
                        if ((dateFormat.parse(dateFormat.format(date)).equals(dateFormat.parse(v)))) { // curTime == varTime
                            break;
                        } if (dateFormat.parse(dateFormat.format(date)).before(dateFormat.parse(v))) { // curTime < varTime
                            break;
                        } value = i+1;
                    } catch (ParseException e) { e.printStackTrace(); }
                }
                arrivalTimes.smoothScrollToPositionFromTop(value,0,10);
                arrivalTimes.setSelection(value);
                Toast.makeText(getActivity(), String.format("Next train arrives at %s", arrivalTimes.getItemAtPosition(value).toString()), Toast.LENGTH_LONG).show(); //debug
                /// . . . List View END . . . ///
                arrivalsGeneralAdapter.notifyDataSetChanged();
            }
        });
        // Initialize Bottom Sheet Parameters
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
        if (!MainActivity.Global.internet) {
            Toast.makeText(getActivity(),
                    "NO INTERNET CONNECTION",
                    Toast.LENGTH_LONG).show();
            sheetBehavior.setPeekHeight(0);
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (MainActivity.Global.internet) {
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
    // Callable function to initialize python3
    private void initPython() {
        if (!Python.isStarted()) { Python.start(new AndroidPlatform(getActivity())); }
    }
    // Python functions: check for internet connection, special schedule, and list schedules
/*    private boolean isSpecial() {
        Python python = Python.getInstance();
        PyObject pythonFile = python.getModule("gtfs");
        return pythonFile.callAttr("isSpecial").toBoolean();
    } private PyObject listSchedules(int fromChoice, int toChoice) {
        Python python = Python.getInstance();
        PyObject pythonFile = python.getModule("gtfs");
        return pythonFile.callAttr("forJava", fromChoice, toChoice );
    }*/
    // initialize button in action bar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mymenu, menu);
    }
    // set actionbar button as reverseButton and create tasks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.reverseButton){
            Toast.makeText(getActivity(),
                    "REVERSE REVERSE",
                    Toast.LENGTH_LONG).show();
            int temp = fromSelection;
            fromSelection = toSelection;
            toSelection = temp;
            background(fromSelection, toSelection);
            // populate list from pyObject listSchedules
            for (int i=0; i<travelTimes.size(); i++) {
                String res = travelTimes.get(i).toJava(String.class);
                arrivals.add(res);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public void background(int fromSelection, int toSelection) {
        Context context = getContext();
        Runnable bgRunnable = new Runnable() {
            Message bgMessage = bgHandler.obtainMessage();
            Bundle bgBundle = new Bundle();

            @Override
            public void run() {

                try {
                    Python python = Python.getInstance();
                    PyObject pythonFile = python.getModule("gtfs");
                    special = pythonFile.callAttr("isSpecial").toBoolean();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Python python = Python.getInstance();
                    PyObject pythonFile = python.getModule("gtfs");
                    List<PyObject> travelTimes = pythonFile.callAttr("forJava", fromChoice, toChoice);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread bgThread = new Thread(bgRunnable);
        bgThread.start();
    }
}