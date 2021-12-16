package com.davidp799.patcotoday.ui.schedules;

import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SchedulesFragment extends Fragment {

    private FragmentSchedulesBinding binding;

    // progress bar
    private Handler handler = new Handler();

    // global variables for arrivals and from and to selections
    int fromSelection, toSelection;
    ArrayList<String> arrivals = new ArrayList<String>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // initial parameters
        binding = FragmentSchedulesBinding.inflate(inflater, container, false);
        initPython(); // initialize python3
        View root = binding.getRoot();

        // allow button in action bar and initialize progress bar
        setHasOptionsMenu(true);

        // from exposed-dropdown-menu
        ArrayList<String> stationsList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.stations_list)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, stationsList); // create array adapter and pass parameters (context, dropdown layout, array)
        AutoCompleteTextView autocompleteTV = root.findViewById(R.id.fromTextView); // get reference to autocomplete text view
        autocompleteTV.setAdapter(arrayAdapter); // set adapter to autocomplete tv to arrayAdapter
        autocompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override // save from selection as variable
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromSelection = position;
                Toast.makeText(getActivity(), stationsList.get(position), Toast.LENGTH_LONG).show();
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
                Toast.makeText(getActivity(), stationsList.get(position), Toast.LENGTH_LONG).show();
                // populate list
                List<PyObject> travelTimes = listSchedules(fromSelection, toSelection).asList();
                // populate list from pyObject listSchedules
                for (int i=0; i<travelTimes.size(); i++) {
                    String res = travelTimes.get(i).toJava(String.class);
                    arrivals.add(res);
                }
                // arrivals ListView
                ListView arrivalTimes = (ListView) root.findViewById(R.id.arrivalTimes);
                ArrayAdapter<String> arrivalsGeneralAdapter = new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_list_item_1,
                        arrivals
                );
                arrivalTimes.setAdapter(arrivalsGeneralAdapter);
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
        if (MainActivity.Global.internet == false) {
            Toast.makeText(getActivity(),
                    "Unable to Check for Special Schedules!\nNO INTERNET CONNECTION",
                    Toast.LENGTH_LONG).show();
            sheetBehavior.setPeekHeight(0);
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (MainActivity.Global.internet == true) {
            boolean specialSchedule = isSpecial();
            // Initialize web view for bottom sheet
            WebView webView = (WebView) root.findViewById(R.id.webview);
            WebSettings webSettings = webView.getSettings();
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            webView.loadUrl("http://www.ridepatco.org/schedules/schedules.asp");
            if (!specialSchedule) {
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
    private boolean isSpecial() {
        Python python = Python.getInstance();
        PyObject pythonFile = python.getModule("gtfs");
        return pythonFile.callAttr("isSpecial").toBoolean();
    } private PyObject listSchedules(int fromChoice, int toChoice) {
        Python python = Python.getInstance();
        PyObject pythonFile = python.getModule("gtfs");
        return pythonFile.callAttr("forJava", fromChoice, toChoice );
    }
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
            List<PyObject> travelTimes = listSchedules(fromSelection, toSelection).asList();
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
}