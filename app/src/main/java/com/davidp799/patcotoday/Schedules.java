package com.davidp799.patcotoday;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Schedules {
    /* Class which utilizes urllib and ZipFile to download the latest
       PortAuthorityTransitCorporation (PATCO) GTFS package.
       Returns: ZipFile object */
    File fileDir = new File("/data/data/com.davidp799.patcotoday/files/data");
    public ArrayList<String> main(Context context, int source_id, int destination_id) {
        List<String> stopCodes = Arrays.asList( "Lindenwold", "Ashland", "Woodcrest", "Haddonfield", "Westmont",
                "Collingswood", "Ferry Avenue", "Broadway", "City Hall", "8th and Market",
                "9-10th and Locust", "12-13th and Locust", "15-16th and Locust" );
        List<Integer> timeBetween = Arrays.asList(0, 2, 3, 6, 8, 10, 12, 16, 18, 24, 26, 27, 28); // |idx(src) - idx(dst)| = travelTime
        List<String> calCodes = Arrays.asList("1,1,1,1,1,0,0", "1,1,1,1,1,0,0", "1,1,1,1,1,0,0", "1,1,1,1,1,0,0",
                "1,1,1,1,1,0,0", "0,0,0,0,0,1,0", "0,0,0,0,0,0,1");
        Calendar cal = Calendar.getInstance();
        int weekday = cal.get(Calendar.DAY_OF_WEEK)-1; // weekday in java starts on sunday
        String source = stopCodes.get(source_id);//source_id-1
        int travelTime = Math.abs(timeBetween.get(source_id) - timeBetween.get(destination_id));
        int route_id;
        if (destination_id < source_id) {
            route_id = 1;
        } else { route_id = 2; }
        // create dependent variables
        int stop_id = stopCodes.indexOf(source)+1;
        ArrayList<String> tripID = trip_id(route_id, service_id(calCodes, weekday));
        ArrayList<String> schedules = listSchedules(tripID, stop_id);
        return formatSchedules(schedules, travelTime);
    }
    public ArrayList<Integer> service_id(List<String> calCodes, int weekday) {
        /* Function which determines the service_id based on day of week.
           Returns: int object */
        String code = calCodes.get(weekday);
        ArrayList<Integer> result = new ArrayList<>();
        if (code.equals("1,1,1,1,1,0,0")) {
            result.add(50); // debug
            result.add(69);
        } else if (code.equals("0,0,0,0,0,1,0")) {
            result.add(51); // debug
            result.add(70);
        } else {
            result.add(52); // debug
            result.add(71);
        } return result;
    }
    public ArrayList<String> trip_id(int route_id, ArrayList<Integer> service_ids) {
        /* Function which determines the trip_id based on the given route_id and service_id
           by reading the trips data file.
           Returns: list object */
        ArrayList<String> trips = new ArrayList<>();
        String filename = fileDir + "/trips.txt";
        // open trips.txt file as r/o data
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                List<String> c = Arrays.asList(line.split(",", 128));
                for (int i=0; i<service_ids.size(); i++) {
                    if (c.get(0).equals(String.valueOf(route_id)) && c.get(1).equals(String.valueOf(service_ids.get(i)))) {
                        trips.add(String.valueOf(c.get(2)));
                    }
                } line = reader.readLine();
            } reader.close();
        } catch (IOException e) {
            Log.d("READ ERROR", "trips.txt");
            e.printStackTrace();
        }
        return trips;
    }
    public ArrayList<String> listSchedules(ArrayList<String> trip_id, int stop_id) {
        /* Function which utilizes urllib to determine if a special schedule is
           present for the current date. */
        // init variables
        ArrayList<String> result = new ArrayList<>();
        // open stop_times dataset and search for arrival times
        String filename = fileDir + "/stop_times.txt";
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                // split line and check if it contains a relevant travel time
                String[] split = line.split(",", 32);
                if (trip_id.contains(split[0])) {
                    if (split[3].equals(String.valueOf(stop_id))) {
                        String[] splitTime = split[1].split(":", 16);
                        result.add(splitTime[0] + ":" + splitTime[1]);
                    }
                } line = reader.readLine();
            } reader.close();
        } catch (IOException e) {
            Log.d("READ ERROR", "stop_times.txt");
            e.printStackTrace();
        } Collections.sort(result);
        return result;
    }
    public ArrayList<String> formatSchedules(ArrayList<String> schedules, int travelTime) {
        /* Function which formats list of trip by removing duplicate arrivals,
           settings arrivals to 12 hour format, and appending travel times. */
        final long MILLISECONDS = 60000;//millisecs

        for (int i=0; i<schedules.size(); i++) {
            String aTime = schedules.get(i);
            String[] split = aTime.split(":",8);
            int curMin = Integer.parseInt(split[1]);
            // remove duplicates
            if (i < schedules.size()-1) {
                String nextTime = schedules.get(i+1);
                String[] nextSplit = nextTime.split(":", 8);
                int nextMin = Integer.parseInt(nextSplit[1]);
                if (nextMin < curMin+3 && nextMin > curMin-3) { // not sure of acceptance interval yet
                    schedules.remove(i+1);
                }
            }
            try {
                // convert to dateTime object, format as 24hr time
                String _24HourTime = schedules.get(i);
                SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm", Locale.US);
                SimpleDateFormat _12HourSDF = new SimpleDateFormat("h:mm a", Locale.US);
                Date _24HourDt = _24HourSDF.parse(_24HourTime);
                // compute trip finish time from train arrival time
                assert _24HourDt != null;
                Date arrivedDt = new Date(_24HourDt.getTime() + (travelTime * MILLISECONDS));
                // append formatted arrival times
                String result = String.format("%s        -        %s", _12HourSDF.format(_24HourDt), _12HourSDF.format(arrivedDt));
                schedules.set(i, result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } return schedules;
    }
}
