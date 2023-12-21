package com.davidp799.patcotoday.utils;

import android.content.Context;
import android.content.res.AssetManager;
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

    /** Returns the length in minutes between source station and destination station.
     * @return integer value of distance in minutes */
    public int getTravelTime(int source_id, int destination_id) {
        return Math.abs(timeBetween.get(source_id) - timeBetween.get(destination_id));
    }

    /** Returns the travel schedule of the train.
     * @return string trip id (weekdays-, saturdays-, sundays-) */
    public String getTripId() {
        if (dayOfWeekNumber >= 1 && dayOfWeekNumber <= 5) { // weekdays
            return "weekdays-";
        } else if (dayOfWeekNumber == 6) { // saturdays
            return "saturdays-";
        } else { // sundays
            return "sundays-";
        }
    }
    /** Returns the direction of the train.
     * @return integer route code (1 = Westbound, 2 = Eastbound) */
    public String getRouteID(String trip_id, int source_id, int destination_id) {
        if (destination_id > source_id) {
            return trip_id + "west.csv";
        } else {
            return trip_id + "east.csv";
        }
    }

    /** Returns the arrival times for the source station using the route_id and source_id.
     * @return ArrayList of strings containing arrival times for the source station */
    public ArrayList<String> getSchedulesList(Context context, String route_id, int source_id) {
        AssetManager am = context.getAssets();
        ArrayList<String> result = new ArrayList<>();
        BufferedReader reader;
        InputStream databaseStream;
        try {
            databaseStream = am.open(route_id);
            reader = new BufferedReader(new InputStreamReader(databaseStream));
            String line = reader.readLine();
            while ( line != null ) {
                ArrayList<String> newC = new ArrayList<>();
                String[] split = line.split(",", 128);
                for (String oldString : split) {
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
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    /** Removes duplicates, converts to 12 hour format, and appends times as Arrival objects.
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
                if (i < schedules.size()-1 && i > 0) {
                    String nextTime = schedules.get(i+1);
                    if (currentTime.equals("99:99")) {
                        schedules.set(i, schedules.get(i-1));
                    } else if (nextTime.equals("99:99")) {
                        schedules.set(i+1, schedules.get(i));
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

