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
        int weekday = cal.get(Calendar.DAY_OF_WEEK);
        String source = stopCodes.get(source_id);//source_id-1
        String destination = stopCodes.get(destination_id);//destination_id-1
        int route_id;
        if (destination_id < source_id) {
            route_id = 1;
        } else { route_id = 2; }
        // create dependent variables
        int stop_id = stopCodes.indexOf(source)+1;
        String time = startTime(stop_id);
        List<Integer> tripID = trip_id(route_id, service_id(calCodes, weekday), weekday);
        List<String> schedules = listSchedules(time, tripID, stop_id);
        ArrayList<String> sortedSchedules = sortedSchedules(schedules);
        return sortedSchedules;
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
    public Integer service_id(List<String> calCodes, int weekday) {
        /* Function which determines the service_id based on day of week.
           Returns: int object */
        int result = 0;
        String filename = String.valueOf(fileDir) + "/calendar.txt";

        // open calendar.txt file as r/o data
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                // do something
                List c = Arrays.asList(line.split(",", 16));
                String str = String.format("%s,%s,%s,%s,%s,%s,%s", c.get(1),c.get(2),c.get(3),
                        c.get(4), c.get(5),c.get(6),c.get(7));
                if (str.equals(calCodes.get(weekday-1))) {
                    result = Integer.parseInt(String.valueOf(c.get(0)));
                } line = reader.readLine();
            } reader.close();
        } catch (IOException e) {
            Log.d("READ ERROR", "calendar.txt");
            e.printStackTrace();
        }
        return result;
    }
    public List<Integer> trip_id(int route_id, int service_id, int weekday) {
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
                // do something
                List c = Arrays.asList(line.split(",", 128));
                if (c.get(0).equals(String.valueOf(route_id)) && c.get(1).equals(String.valueOf(service_id))) {
                    trips.add(Integer.parseInt(String.valueOf(c.get(2))));
                } line = reader.readLine();
            } reader.close();
        } catch (IOException e) {
            Log.d("READ ERROR", "trips.txt");
            e.printStackTrace();
        }
        return trips;
    }
    public List<String> listSchedules(String startTime, List trip_id, int stop_id) {
        /* Function which utilizes urllib to determine if a special schedule is
           present for the current date. */
        // init variables
        List allTrips = new ArrayList();
        for (Object i : trip_id) {
            allTrips.add(String.valueOf(i));
        }
        List temp = new ArrayList();
        List allTimes = new ArrayList();
        List result = new ArrayList();

        // open stop_times dataset and search for arrival times
        String filename = String.valueOf(fileDir) + "/stop_times.txt";
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                // do something
                List c = Arrays.asList(line.split(",", 256));
                if (allTrips.contains(c.get(0))) {
                    temp.add(line);
                } line = reader.readLine();
            } reader.close();
        } catch (IOException e) {
            Log.d("READ ERROR", "stop_times.txt");
            e.printStackTrace();
        } for (Object i : temp) { // extract arrival times from strings
            List split = Arrays.asList(String.valueOf(i).split(",", 16));
            if (split.get(3).equals(String.valueOf(stop_id))) {
                allTimes.add(split.get(1));
            }
        } for (Object i : allTimes ) { // extract arrival times beginning at current time from allTimes
            // initialize long variables
            int iHour = Integer.parseInt(String.valueOf(i).substring(0,1));
            int startHour = Integer.parseInt(String.valueOf(startTime).substring(0,1));
            int iMinute = Integer.parseInt(String.valueOf(i).substring(3,4));
            int startMinute = Integer.parseInt(String.valueOf(startTime).substring(3,4))-5;
            // if current hour and >= cur min-5
            if ( iHour == startHour && iMinute >= startMinute ) {
                result.add(i);
            } else if ( iHour >= startHour ) {
                result.add(i);
            }
        } return allTimes;
    }
    public ArrayList<String> sortedSchedules(List schedules) {
        /* Function which sorts list of trip by calling trip_id() and utilizes
           RADIX sort method to sort arrival times in ascending order. */
        List temp = new ArrayList();
        ArrayList<String> result = new ArrayList();
        for (Object i : schedules) {
            temp.add(String.valueOf(i).substring(0,5));
        } Collections.sort(temp);
        for (Object i : temp) {
            List split = Arrays.asList(String.valueOf(i).split(":",8));
            if (Integer.parseInt(String.valueOf(split.get(0))) == 12) {
                result.add(String.format("%s:%s PM", split.get(0), split.get(1)));
            } else if (String.valueOf(split.get(0)).equals("00")) {
                result.add(String.format("12:%s AM", split.get(1)));
            } else if (Integer.parseInt(String.valueOf(split.get(0))) > 12) {
                result.add(String.format("%s:%s PM", Integer.parseInt(String.valueOf(split.get(0)))-12, split.get(1)));
            } else {
                result.add(String.format("%s:%s AM", Integer.parseInt(String.valueOf(split.get(0))), split.get(1)));
            }
        } return result;
    }
}
