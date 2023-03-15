package com.davidp799.patcotoday.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// TODO: update gtfs.zip gathering to include modified calendar.txt (with 1-6 lines it works)
/** Class which analyzes PATCO Transit GTFS data to provide a list of upcoming arrivals
 *  based on provided source and destination stations and day of week. */
public class Schedules {
    private final List<Integer> timeBetween = Arrays.asList(0, 2, 3, 6, 8, 10, 12, 16, 18, 24, 26, 27, 28);
    // Codes representing weekday or weekend status; Used to determine service_id
    private final int dayOfWeekNumber = LocalDate.now().getDayOfWeek().getValue();


    /** Function which returns the length in minutes between source station and destination station.
     * @return integer value of distance in minutes */
    public int getTravelTime(int source_id, int destination_id) {
        return Math.abs(timeBetween.get(source_id) - timeBetween.get(destination_id));
    }
    /** Function which returns the routeID which represents the direction
     *  of the desired train (Eastbound / Westbound).
     * @return integer route code (1 = Westbound, 2 = Eastbound) */
    public ArrayList<Integer> getTripId() {
        ArrayList<Integer> result = new ArrayList<>();
        if (dayOfWeekNumber >= 1 && dayOfWeekNumber <= 5) { // weekday, start at line 2 and end at 92 (idx start at 0)
            result.add(2);
            result.add(92);
        } else if (dayOfWeekNumber == 6) { // saturday, start at line 94 and end at 153
            result.add(94);
            result.add(153);
        } else { // sunday, start at line 155 and end at 197
            result.add(155);
            result.add(197);
        } return result;
    }
    /** Function which returns the routeID which represents the direction
     *  of the desired train (Eastbound / Westbound).
     * @return integer route code (1 = Westbound, 2 = Eastbound) */
    public int getRouteID(int source_id, int destination_id) {
        System.out.println("$$$: source, dest id = " + source_id + ", " + destination_id);
        if (source_id < destination_id) {
            return 2;
        } else { return 1; }
    }

    /** Function which reads the stop_times.txt data file to determine the
     *  trip_id based on the given route_id and service_id.
     * @return ArrayList of strings */
    public ArrayList<String> getSchedulesList(Context context, int route_id, int source_id) {
        AssetManager am = context.getAssets();
        ArrayList<String> result = new ArrayList<>();
        ArrayList<Integer> tripId = getTripId();
        BufferedReader reader;
        InputStream databaseStream = null;
        try {
            databaseStream = am.open("arrivals_database.csv");
            reader = new BufferedReader(new InputStreamReader(databaseStream));
            String line = reader.readLine();
            System.out.println("$$$: trip id = " + tripId);
            int lineIndex = 0;
            while ( line != null ) {
                if (lineIndex >= tripId.get(0) && lineIndex <= tripId.get(1)) {
                    List<String> c;
                    ArrayList<String> newC = new ArrayList<>();
                    String[] split = line.split("-1,");
                    System.out.println("$$$ currentLine = " + line);
                    if (route_id == 1) { // return westbound
                        c = Arrays.asList(split[1].split(",", 128));
                    } else {
                        c = Arrays.asList(split[0].split(",", 128));
                    }
                    for (int j=0; j<c.size(); j++) {
                        String oldString = c.get(j);
                        if (oldString.contains("A")) {
                            String newString = oldString.replace("A", "");
                            newC.add(newString);
                        } else if (oldString.contains("P") && !oldString.contains("12:")) {
                            String newString = oldString.replace("P", "");
                            String[] newParts = newString.split(":");
                            int newHour = Integer.parseInt(newParts[0]) + 12;
                            newString = newHour + ":" + newParts[1];
                            newC.add(newString);
                        } else if (oldString.contains("P") && oldString.contains("12:")) {
                            String newString = oldString.replace("P", "");
                            newC.add(newString);
                        }
                    }
                    result.add(newC.get(source_id));
                }
                line = reader.readLine();
                lineIndex += 1;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("$$$ = " + result);
        return result;
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
            String currentTime = schedules.get(i);
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm", Locale.US);
            try {
                Date current24HourDt = _24HourSDF.parse(currentTime);
                if (i < schedules.size()-1 && i > 0) {
                    String prevTime = schedules.get(i-1);
                    String nextTime = schedules.get(i+1);
                    if (currentTime.equals("99:99")) {
                        schedules.set(i, schedules.get(i-1));
                        System.out.println("$$$ Error: 99:99 time found, parsed as = " + _24HourSDF.parse(nextTime));
                    } else if (nextTime.equals("99:99")) {
                        schedules.set(i+1, schedules.get(i));
                        System.out.println("$$$ Error: 99:99 time found, parsed as = " + _24HourSDF.parse(nextTime));
                    }
                }
                // convert to dateTime object, format as 24hr time
                String _24HourTime = schedules.get(i);
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
        }
        return arrivals;
    }


}

