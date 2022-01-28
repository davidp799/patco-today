package com.davidp799.patcotoday.ui.schedules;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.davidp799.patcotoday.R;
import com.davidp799.patcotoday.Schedules;
import com.davidp799.patcotoday.databinding.FragmentSchedulesBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.transition.MaterialFadeThrough;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SchedulesFragment extends Fragment {
    // BINDING //
    private FragmentSchedulesBinding binding;
    // LIST VIEW DEFAULT VALUES //
    private int fromSelection=1, toSelection=11;
    private Schedules schedules = new Schedules();
    private ArrayList<String> arrivals = schedules.main(fromSelection, toSelection);
    // BACKGROUND THREAD VALUES //
    private Document doc;
    private boolean internet;
    private boolean special;
    private boolean downloaded;
    private boolean extracted;
    // BEGIN HANDLERS //
    Handler downloadHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle downloadBundle = msg.getData();
            boolean myMessage = downloadBundle.getBoolean("MSG_KEY");

            downloaded = msg.getData().getBoolean("MSG_KEY");
        }
    }; Handler extractHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle extractBundle = msg.getData();
            boolean myMessage = extractBundle.getBoolean("MSG_KEY");

            extracted = msg.getData().getBoolean("MSG_KEY");
            if (extracted) {
                Toast.makeText(getActivity(), String.format("Files up to Date", extracted), Toast.LENGTH_SHORT).show();
            }
        }
    }; Handler internetHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle internetBundle = msg.getData();
            boolean myMessage = internetBundle.getBoolean("MSG_KEY");

            internet = msg.getData().getBoolean("MSG_KEY");
            Toast.makeText(getActivity(), String.format("Internet: %s", internet), Toast.LENGTH_SHORT).show();
        }
    }; Handler specialHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle objBundle = msg.getData();
            boolean myMessage = objBundle.getBoolean("MSG_KEY");

            special = msg.getData().getBoolean("MSG_KEY");
            Toast.makeText(getActivity(), String.format("Special: %s", special), Toast.LENGTH_SHORT).show();
        }
    };
    // ON CREATE VIEW //
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSchedulesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);
        setEnterTransition(new MaterialFadeThrough());
        // BACKGROUND THREAD //
        //
        // DOWNLOAD ZIP //
        File fileDir = new File("/data/data/" + getActivity().getPackageName() + "/files/gtfs");
        fileDir.mkdirs();
        // http://transitfeeds.com/p/patco/533/latest/download
        downloadZip("http://www.ridepatco.org/developers/PortAuthorityTransitCorporation.zip",
                "/data/data/" + getActivity().getPackageName() + "/files/gtfs/gtfs.zip");
        // EXTRACT ZIP //
        extractZip("/data/data/" + getActivity().getPackageName() + "/files/gtfs/gtfs.zip");
        if (extracted) {
            Toast.makeText(getContext(), "Files up to Date", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Unable to Update Files", Toast.LENGTH_SHORT).show();
        }
        //
        // LIST VIEW
        ListView arrivalsListView = (ListView) root.findViewById(R.id.arrivalsListView);
        ArrayAdapter<String> arrivalsGeneralAdapter = new ArrayAdapter<>( getActivity(), android.R.layout.simple_list_item_1, arrivals );
        arrivalsListView.setAdapter(arrivalsGeneralAdapter);
        ArrayList<String> travelTimes = schedules.main(fromSelection, toSelection); // populate list
        // populate list from pyObject listSchedules
        arrivals.addAll(travelTimes);
        // scroll to next train PARTIALLY FUNCTIONAL
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm aa") ;
        dateFormat.format(date);
        //
        int value = 0;
        for (int i = 0; i < arrivalsListView.getCount(); i++) {
            String v = String.valueOf(travelTimes.get(i));
            try {
                if ((dateFormat.parse(dateFormat.format(date)).equals(dateFormat.parse(v)))) { // curTime == varTime
                    break;
                } if (dateFormat.parse(dateFormat.format(date)).before(dateFormat.parse(v))) { // curTime < varTime
                    break;
                } value = i+1;
            } catch (ParseException e) { e.printStackTrace(); }
        }
        arrivalsListView.smoothScrollToPositionFromTop(value,0,10);
        arrivalsListView.setSelection(value);
        /// . . . List View END . . . ///
        //
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
                ListView arrivalsListView = (ListView) root.findViewById(R.id.arrivalsListView); // init arrivals listview
                ArrayAdapter<String> arrivalsGeneralAdapter = new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_list_item_1,
                        arrivals
                );
                arrivalsListView.setAdapter(arrivalsGeneralAdapter);

                ArrayList<String> travelTimes = schedules.main(fromSelection, toSelection); // populate list
                // populate list from pyObject listSchedules
                arrivals.addAll(travelTimes);

                // scroll to next train PARTIALLY FUNCTIONAL
                Date date = new Date() ;
                SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm aa") ;
                dateFormat.format(date);

                int value = 0;
/*                for (int i = 0; i < arrivalsListView.getCount(); i++) {
                    String v = String.valueOf(travelTimes.get(i));
                    try {
                        if ((dateFormat.parse(dateFormat.format(date)).equals(dateFormat.parse(v)))) { // curTime == varTime
                            break;
                        } if (dateFormat.parse(dateFormat.format(date)).before(dateFormat.parse(v))) { // curTime < varTime
                            break;
                        } value = i+1;
                    } catch (ParseException e) { e.printStackTrace(); }
                }*/
                arrivalsListView.smoothScrollToPositionFromTop(value,0,10);
                arrivalsListView.setSelection(value);
                arrivalsGeneralAdapter.notifyDataSetChanged();

                if (value > 0) {
                    Toast.makeText(getActivity(), String.format("Next train arrives at %s", arrivalsListView.getItemAtPosition(value).toString()), Toast.LENGTH_LONG).show(); //debug
                }

                // DEBUG //
                Toast.makeText(getActivity(), String.format("DEBUG: travelTimes.size is %s", travelTimes.size()), Toast.LENGTH_LONG).show();
                // DEBUG //
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
        if (!internet) {
            Toast.makeText(getActivity(),
                    "NO INTERNET CONNECTION",
                    Toast.LENGTH_LONG).show();
            sheetBehavior.setPeekHeight(0);
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (internet) {
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
        } return root;
    }
    // initialize button in action bar
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.reversemenu, menu);
    }
    // set actionbar button as reverseButton and create tasks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.reverseButton){
            if (fromSelection > toSelection) {
                Toast.makeText(getActivity(), "Now Going: Westbound", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Now Going: Eastbound", Toast.LENGTH_LONG).show();
            }
            int temp = fromSelection;
            fromSelection = toSelection;
            toSelection = temp;
            ArrayList<String> travelTimes = schedules.main(fromSelection, toSelection);
            // populate list from pyObject listSchedules
            arrivals.addAll(travelTimes);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    // BACKGROUND THREAD TASKS //
    public void checkInternet(View view) {
        Runnable internetRunnable = new Runnable() {
            Message internetMessage = internetHandler.obtainMessage();
            Bundle internetBundle = new Bundle();
            @Override
            public void run() {
                try {
                    String command = "ping -c 1 www.ridepatco.org";
                    internet = (Runtime.getRuntime().exec(command).waitFor() == 0);
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
            Message specialMessage = specialHandler.obtainMessage();
            Bundle specialBundle = new Bundle();
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
                StringBuilder headBuilder = new StringBuilder();
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
        specialBgThread.start();
    } public void downloadZip(String urlStr, String destinationFilePath) {
        Context context = getContext();
        Runnable downloadRunnable = new Runnable() {
            Message downloadMessage = downloadHandler.obtainMessage();
            Bundle downloadBundle = new Bundle();
            @Override
            public void run() {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urlStr);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.d("downloadZipFile", "Server ResponseCode=" + connection.getResponseCode() + " ResponseMessage=" + connection.getResponseMessage());
                    }
                    // download the file
                    input = connection.getInputStream();
                    Log.d("downloadZipFile", "destinationFilePath=" + destinationFilePath);
                    new File(destinationFilePath).createNewFile();
                    output = new FileOutputStream(destinationFilePath);

                    byte data[] = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
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
                Log.d("downloadZipFile", "f.getParentFile().getPath()=" + f.getParentFile().getPath());
                Log.d("downloadZipFile", "f.getName()=" + f.getName().replace(".zip", ""));
            }
        };
        Thread downloadBgThread = new Thread(downloadRunnable);
        downloadBgThread.start();
    } public void extractZip(String filePath) {
        Context context = getContext();
        Runnable extractRunnable = new Runnable() {
            Message extractMessage = extractHandler.obtainMessage();
            Bundle extractBundle = new Bundle();
            @Override
            public void run() {
                InputStream is;
                ZipInputStream zis;
                try {
                    File zipfile = new File(filePath);
                    String parentFolder = zipfile.getParentFile().getPath();
                    String filename;
                    is = new FileInputStream(filePath);
                    zis = new ZipInputStream(new BufferedInputStream(is));
                    ZipEntry ze;
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((ze = zis.getNextEntry()) != null) {
                        filename = ze.getName();
                        if (ze.isDirectory()) {
                            File fmd = new File(parentFolder + "/" + filename);
                            fmd.mkdirs();
                            continue;
                        } FileOutputStream fout = new FileOutputStream(
                                parentFolder + "/" + filename);
                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }
                        fout.close();
                        zis.closeEntry();
                    } zis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    extracted = false;
                }
                extracted = true;
            }
        };
        Thread extractBgThread = new Thread(extractRunnable);
        extractBgThread.start();
    }
}