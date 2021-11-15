package com.davidp799.patcotoday.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.davidp799.patcotoday.R;
import com.davidp799.patcotoday.databinding.FragmentHomeBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // initial parameters
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        initPython(); // initialize python3
        View root = binding.getRoot();

        // allow button in action bar
        setHasOptionsMenu(true);

        // from exposed-dropdown-menu
        String[] stations = getResources().getStringArray(R.array.stations_list); // get reference to string array
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, stations); // create array adapter and pass parameters (context, dropdown layout, array)
        AutoCompleteTextView autocompleteTV = root.findViewById(R.id.fromTextView); // get reference to autocomplete text view
        autocompleteTV.setAdapter(arrayAdapter); // set adapter to autocomplete tv to arrayAdapter
        // to exposed-dropdown-menu
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, stations); // create array adapter and pass parameters (context, dropdown layout, array)
        AutoCompleteTextView autocompleteTV2= root.findViewById(R.id.toTextView); // get reference to autocomplete text view
        autocompleteTV2.setAdapter(arrayAdapter2); // set adapter to autocomplete tv to arrayAdapter




        /* // Initialize webview for bottom sheet
        WebView webView = (WebView) root.findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webView.loadUrl("http://www.ridepatco.org/schedules/schedules.asp"); */

        // Initialize Bottom Sheet Parameters
        LinearLayout mBottomSheetLayout = root.findViewById(R.id.bottom_sheet_layout);
        BottomSheetBehavior<LinearLayout> sheetBehavior;
        ImageView header_Arrow_Image;
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

        // Determine if special schedule is present for current date
        boolean specialSchedule = isSpecial();
        if (!specialSchedule) {
            sheetBehavior.setPeekHeight(0);
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        return root;
    }

    // Callable function to initialize python3
    private void initPython() {
        if (!Python.isStarted()) { Python.start(new AndroidPlatform(getActivity())); }
    }

    // Python function which determines whether there is a special schedule
    private boolean isSpecial() {
        Python python = Python.getInstance();
        PyObject pythonFile = python.getModule("gtfs");
        return pythonFile.callAttr("isSpecial").toBoolean();
    }

    // initialize button in action bar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mymenu, menu);
    }
    // set actionbar buttion as reverseButton and create tasks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.reverseButton){
            //What you want(Code Here)
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