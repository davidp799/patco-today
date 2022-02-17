package com.davidp799.patcotoday;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    public ArrayList<String> main(int source_id, int destination_id) {
        List<String> stopCodes = Arrays.asList( "Lindenwold", "Ashland", "Woodcrest", "Haddonfield", "Westmont", //b5 c7
                "Collingswood", "Ferry Avenue", "Broadway", "City Hall", "8th and Market",
                "9-10th and Locust", "12-13th and Locust", "15-16th and Locust" );
        List<String> calCodes = Arrays.asList("1,1,1,1,1,0,0", "1,1,1,1,1,0,0", "1,1,1,1,1,0,0", "1,1,1,1,1,0,0",
                "1,1,1,1,1,0,0", "0,0,0,0,0,1,0", "0,0,0,0,0,0,1");
        Calendar cal = Calendar.getInstance();
        int weekday = cal.get(Calendar.DAY_OF_WEEK)-2; // weekday in java starts on sunday
        String source = stopCodes.get(source_id);//source_id-1
        String destination = stopCodes.get(destination_id);//destination_id-1
        int route_id;
        if (destination_id < source_id) {
            route_id = 1;
        } else { route_id = 2; }
        // create dependent variables
        int stop_id = stopCodes.indexOf(source)+1;
        String time = startTime(stop_id);
        ArrayList<String> tripID = trip_id(route_id, service_id(calCodes, weekday), weekday);
        ArrayList<String> schedules = listSchedules(time, tripID, stop_id);
        return formatSchedules(schedules);
    }
    public List<Integer> time() {
        /* Function which utilizes urllib to determine if a special schedule is
           present for the current date. */
        String now = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new java.util.Date());
        String[] split = now.split(":", 3);
        List<Integer> result = new ArrayList<>();
        for (String str : split) {
            result.add(Integer.parseInt(str));
        } return result;
    }
    public String startTime(int stop_id) {
        /* Function which uses the current time to determine where to begin
           fetching train arrival times.
           Returns: string object */
        List<Integer> now = time();
        List<String> converted = new ArrayList<String>();
        for (Object part : now) {
            String str = part.toString();
            if (str.length() == 1) {
                str = "0"+str;
            } converted.add(str);
        } return String.format("%s:%s:00", converted.get(0), converted.get(1));
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
    public ArrayList<String> trip_id(int route_id, ArrayList<Integer> service_ids, int weekday) {
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
    public ArrayList<String> listSchedules(String startTime, ArrayList<String> trip_id, int stop_id) {
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
    public ArrayList<String> formatSchedules(ArrayList<String> schedules) {
        /* Function which sorts list of trip by calling trip_id() and utilizes
           RADIX sort method to sort arrival times in ascending order. */
        for (int i=0; i<schedules.size(); i++) {
            String aTime = schedules.get(i);
            String[] split = aTime.split(":",8);
            int curMin = Integer.parseInt(split[1]);
            // remove duplicates
            if (i < schedules.size()-1) {
                String nextTime = schedules.get(i+1);
                String[] nextSplit = nextTime.split(":", 8);
                int nextMin = Integer.parseInt(nextSplit[1]);
                if (nextMin < curMin+3 && nextMin > curMin-3) {
                    schedules.remove(i+1);
                }
            }
            // format to 12hour time
            try {
                String _24HourTime = schedules.get(i);
                SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm", Locale.US);
                SimpleDateFormat _12HourSDF = new SimpleDateFormat("h:mm a", Locale.US);
                Date _24HourDt = _24HourSDF.parse(_24HourTime);
                schedules.set(i, _12HourSDF.format(_24HourDt));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } return schedules;
    }
}
