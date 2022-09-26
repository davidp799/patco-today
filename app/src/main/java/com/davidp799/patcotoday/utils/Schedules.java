package com.davidp799.patcotoday.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Class which analyzes PATCO Transit GTFS data to provide a list of upcoming arrivals
 *  based on provided source and destination stations and day of week. */
public class Schedules {
    // Default file directory
    private final File fileDir = new File("/data/data/com.davidp799.patcotoday/files/data");
    // Names for station ID's at specified indices
    private final List<String> stopCodes = Arrays.asList( "Lindenwold", "Ashland", "Woodcrest", "Haddonfield", "Westmont",
            "Collingswood", "Ferry Avenue", "Broadway", "City Hall", "8th and Market",
            "9-10th and Locust", "12-13th and Locust", "15-16th and Locust" );
    // Travel times between stations at specified indices; |idx(src) - idx(dst)| = travelTime
    private final List<Integer> timeBetween = Arrays.asList(0, 2, 3, 6, 8, 10, 12, 16, 18, 24, 26, 27, 28);
    // Codes representing weekday or weekend status; Used to determine service_id
    private final List<String> calCodes = Arrays.asList("1,1,1,1,1,0,0", "1,1,1,1,1,0,0", "1,1,1,1,1,0,0", "1,1,1,1,1,0,0",
            "1,1,1,1,1,0,0", "0,0,0,0,0,1,0", "0,0,0,0,0,0,1");

    /** Function which returns the name of the given station index.
     * @param source_id station corresponding to stopCodes index
     * @return string value of station name */
    public String getStopCode(int source_id) {
        return stopCodes.get(source_id);
    }
    /** Function which returns the length in minutes between source
     *  station and destination station.
     * @param source_id starting point
     * @param destination_id ending point
     * @return integer value of distance in minutes */
    public int getTravelTime(int source_id, int destination_id) {
        return Math.abs(timeBetween.get(source_id) - timeBetween.get(destination_id));
    }
    /** Function which returns the routeID which represents
     *  the direction of the desired train (Eastbound / Westbound).
     * @param source_id starting point
     * @param destination_id ending point
     * @return integer route code (1 = Westbound, 2 = Eastbound) */
    public int getRouteID(int source_id, int destination_id) {
        if (destination_id < source_id) {
            return 1;
        } else { return 2; }
    }

    /** Function which returns the service_id and determines whether a
     *  weekday or weekend schedule is used.
     * @param weekday integer value representing day of week
     * @return ArrayList of integers */
    public ArrayList<Integer> getServiceID(int weekday) {
        String code = calCodes.get(weekday-1);
        System.out.println("@code = " + code);
        int weekdayId = 0, saturdayId = 0, sundayId = 0;
        ArrayList<Integer> result = new ArrayList<>();
        String filename = fileDir + "/calendar.txt";
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                List<String> splitLine = Arrays.asList(line.split(",", 12));
                System.out.println("@line = " + splitLine);
                String currentCode = String.format("%s,%s,%s,%s,%s,%s,%s",
                        splitLine.get(1), splitLine.get(2), splitLine.get(3), splitLine.get(4), splitLine.get(5), splitLine.get(6), splitLine.get(7));
                switch (currentCode) {
                    case "1,1,1,1,1,0,0":
                        weekdayId = Integer.parseInt(splitLine.get(0));
                        break;
                    case "0,0,0,0,0,1,0":
                        saturdayId = Integer.parseInt(splitLine.get(0));
                        break;
                    case "0,0,0,0,0,0,1":
                        sundayId = Integer.parseInt(splitLine.get(0));
                        break;
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } if (code.equals("1,1,1,1,1,0,0")) {
            result.add(weekdayId);
        } else if (code.equals("0,0,0,0,0,1,0")) {
            result.add(saturdayId);
        } else {
            result.add(sundayId);
        }
        System.out.println("@result = " + result);
        return result;
    }

    /** Function which reads the trips.txt data file to determine the
     *  trip_id based on the given route_id and service_id.
     * @param route_id integer value representing travel direction
     * @param service_ids list of service codes which determine which
     *        weekday or weekend schedule is used.
     * @return ArrayList of strings */
    public ArrayList<String> getTripID(int route_id, ArrayList<Integer> service_ids) {
        ArrayList<String> tripIDs = new ArrayList<>();
        String filename = fileDir + "/trips.txt";
        // open trips.txt file as read only
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                // split line, pull service_id and add to tripID list
                List<String> c = Arrays.asList(line.split(",", 128));
                for (int i=0; i<service_ids.size(); i++) {
                    if (c.get(0).equals(String.valueOf(route_id)) && c.get(1).equals(String.valueOf(service_ids.get(i)))) {
                        tripIDs.add(String.valueOf(c.get(2)));
                    }
                } line = reader.readLine();
            } reader.close();
        } catch (IOException e) {
            Log.d("READ ERROR", "trips.txt");
            e.printStackTrace();
        }
        return tripIDs;
    }
    /** Function which reads the stop_times.txt data file to determine the
     *  trip_id based on the given route_id and service_id.
     * @param trip_id list of tripIDs
     * @param source_id integer representing source station (will add 1)
     * @return ArrayList of strings */
    public ArrayList<String> getSchedulesList(ArrayList<String> trip_id, int source_id) {
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
                    if (split[3].equals(String.valueOf(source_id+1))) {
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
    /** Function which formats list of trip by removing duplicate arrivals,
     *  setting arrivals to 12 hour format, and appending travel times.
     *  @param schedules unformatted list of arrival times
     *  @param travelTime minutes between source and destination station
     *  @return ArrayList of strings */
    public ArrayList<String> getFormatString(ArrayList<String> schedules, int travelTime) {
        final long MILLISECONDS = 60000; //milliseconds

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
                schedules.set(i, String.format("%s", _12HourSDF.format(_24HourDt)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } return schedules;
    }
    /** Function which formats list of trip by removing duplicate arrivals,
     *  setting arrivals to 12 hour format, and appending travel times as Arrival objects.
     *  @param schedules unformatted list of arrival times
     *  @param travelTime minutes between source and destination station
     *  @return ArrayList of Arrival objects */
    public ArrayList<Arrival> getFormatArrival(ArrayList<String> schedules, int travelTime) {
        final long MILLISECONDS = 60000; //milliseconds
        ArrayList<Arrival> arrivals = new ArrayList<>();

        for (int i=0; i<schedules.size(); i++) {
            String aTime = schedules.get(i);
            String[] split = aTime.split(":",8);
            try {
                // convert to dateTime object, format as 24hr time
                String _24HourTime = schedules.get(i);
                SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm", Locale.US);
                SimpleDateFormat _12HourSDF = new SimpleDateFormat("h:mm a", Locale.US);
                Date _24HourDt = _24HourSDF.parse(_24HourTime);
                // compute trip finish time from train arrival time
                assert _24HourDt != null;
                Date arrivedDt = new Date(_24HourDt.getTime() + (travelTime * MILLISECONDS));

                // append formatted arrival times as Arrival objects
                Arrival thisArrival = new Arrival(_12HourSDF.format(_24HourDt), _12HourSDF.format(arrivedDt));
                arrivals.add(i, thisArrival);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } return arrivals;
    }

}
