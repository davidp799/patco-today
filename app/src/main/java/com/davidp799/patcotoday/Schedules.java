package com.davidp799.patcotoday;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

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
        List<String> tripID = trip_id(route_id, service_id(calCodes, weekday), weekday);
        List<String> schedules = listSchedules(time, tripID, stop_id);
        ArrayList<String> formattedSchedules = formatSchedules(schedules);
        return formattedSchedules;
    }
    public List<Integer> time() {
        /* Function which utilizes urllib to determine if a special schedule is
           present for the current date. */
        String now = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        List<String> split = Arrays.asList(now.split(":", 3));
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
    public ArrayList service_id(List<String> calCodes, int weekday) {
        /* Function which determines the service_id based on day of week.
           Returns: int object */
        String code = calCodes.get(weekday);
        ArrayList result = new ArrayList();
        if (code.equals("1,1,1,1,1,0,0")) {
            result.add(50);
            result.add(69);
        } else if (code.equals("0,0,0,0,0,1,0")) {
            result.add(51);
            result.add(70);
        } else {
            result.add(52);
            result.add(71);
        } return result;
    }
    public List<String> trip_id(int route_id, ArrayList service_ids, int weekday) {
        /* Function which determines the trip_id based on the given route_id and service_id
           by reading the trips data file.
           Returns: list object */
        List trips = new ArrayList();
        String filename = String.valueOf(fileDir) + "/trips.txt";

        // open trips.txt file as r/o data
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                List c = Arrays.asList(line.split(",", 128));
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
    public ArrayList listSchedules(String startTime, List trip_id, int stop_id) {
        /* Function which utilizes urllib to determine if a special schedule is
           present for the current date. */
        // init variables
        ArrayList result = new ArrayList();

        // open stop_times dataset and search for arrival times
        String filename = String.valueOf(fileDir) + "/stop_times.txt";
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                // do something
                List c = Arrays.asList(line.split(",", 256));
                if (trip_id.contains(c.get(0))) {
                    List split = Arrays.asList(line.split(",", 16));
                    if (split.get(3).equals(String.valueOf(stop_id))) {
                        String aString = String.valueOf(split.get(1)).substring(0,5);
                        result.add(aString);
                    }
                } line = reader.readLine();
            } reader.close();
        } catch (IOException e) {
            Log.d("READ ERROR", "stop_times.txt");
            e.printStackTrace();
        } Collections.sort(result);
        return result;
    }
    public ArrayList<String> formatSchedules(List schedules) {
        /* Function which sorts list of trip by calling trip_id() and utilizes
           RADIX sort method to sort arrival times in ascending order. */
        ArrayList newSchedules = new ArrayList();
        for (Object i : schedules) {
            /*if (schedules.indexOf(i) < schedules.size()-1) {
                List iSplit = Arrays.asList(String.valueOf(i).split(":", 8));
                Object j = schedules.get(schedules.indexOf(i) + 1);
                List jSplit = Arrays.asList(String.valueOf(j).split(":", 8));
                int iMin = Integer.parseInt(String.valueOf(iSplit.get(1)));
                int jMin = Integer.parseInt(String.valueOf(jSplit.get(1)));
                if (!(jMin < iMin + 3 && jMin > iMin - 3)) {
                }
            }*/
            List split = Arrays.asList(String.valueOf(i).split(":",8));
            if (Integer.parseInt(String.valueOf(split.get(0))) == 12) {
                schedules.set(schedules.indexOf(i), String.format("%s:%s PM", split.get(0), split.get(1)));
            } else if (String.valueOf(split.get(0)).equals("00")) {
                schedules.set(schedules.indexOf(i), String.format("12:%s AM", split.get(1)));
            } else if (Integer.parseInt(String.valueOf(split.get(0))) > 12) {
                schedules.set(schedules.indexOf(i), String.format("%s:%s PM", Integer.parseInt(String.valueOf(split.get(0)))-12, split.get(1)));
            } else {
                schedules.set(schedules.indexOf(i), String.format("%s:%s AM", Integer.parseInt(String.valueOf(split.get(0))), split.get(1)));
            }
        } return (ArrayList<String>) schedules;
    }
}
