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
import java.util.Objects;

/** Class which analyzes PATCO Transit GTFS data to provide a list of upcoming arrivals
 *  based on provided source and destination stations and day of week. */
public class Schedules {
    private final List<Integer> timeBetween = Arrays.asList(0, 2, 3, 6, 8, 10, 12, 16, 18, 22, 24, 26, 27, 28);

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
    public String getRouteID(int source_id, int destination_id) {
        if (destination_id > source_id) {
            return getTripId() + "west.csv";
        } else {
            return getTripId() + "east.csv";
        }
    }

    /** Returns the arrival times for the source station using the route_id and source_id.
     * @return ArrayList of strings containing arrival times for the source station */
    public ArrayList<Trip> getSchedulesList(
            Context context, String route_id, int source_id, int destination_id
    ) {
        ArrayList<Trip> schedulesList = new ArrayList<>(); // final result
        AssetManager assetManager = context.getAssets();
        BufferedReader reader;
        InputStream databaseStream;
        try {
            databaseStream = assetManager.open(route_id);
            reader = new BufferedReader(new InputStreamReader(databaseStream));
            String currentLine = reader.readLine();
            while ( currentLine != null ) {
                Trip formattedTrip = new Trip("", false, false);
                String[] unformattedTrips = currentLine.split(",", 128);
                String unformattedTrip = unformattedTrips[source_id];
                // Set arrival time for trip
                if (unformattedTrip.contains("A")) {
                    String newString = unformattedTrip.replace("A", "");
                    formattedTrip.setArrivalTime(newString);
                } else if (unformattedTrip.contains("P") && !unformattedTrip.contains("12:")) {
                    String newString = unformattedTrip.replace("P", "");
                    String[] newParts = newString.split(":");
                    int newHour = Integer.parseInt(newParts[0]) + 12;
                    newString = newHour + ":" + newParts[1];
                    formattedTrip.setArrivalTime(newString);
                } else if (unformattedTrip.contains("P") && unformattedTrip.contains("12:")) {
                    String newString = unformattedTrip.replace("P", "");
                    formattedTrip.setArrivalTime(newString);
                }
                // Set source and destination station status for trip
                if (unformattedTrips[source_id].contains("CLOSED")) {
                    formattedTrip.setSourceClosed(true);
                }
                if (unformattedTrips[destination_id].contains("CLOSED")) {
                    formattedTrip.setDestinationClosed(true);
                }
                schedulesList.add(formattedTrip);
                currentLine = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedulesList;
    }
    /** Removes duplicates, converts to 12 hour format, and appends times as Arrival objects.
     *  @param schedules unformatted list of arrival times
     *  @param travelTime minutes between source and destination station
     *  @return ArrayList of Arrival objects */
    public ArrayList<Arrival> getFormatArrival(ArrayList<Trip> schedules, int travelTime) {
        final long MILLISECONDS = 60000; //milliseconds
        ArrayList<Arrival> arrivals = new ArrayList<>();
        for (int i=0; i<schedules.size(); i++) {
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm", Locale.US);
            try {
                SimpleDateFormat _12HourSDF = new SimpleDateFormat("h:mm a", Locale.US);
                Arrival thisArrival = new Arrival("00:00", "00:00");
                Trip thisTrip = schedules.get(i);
                if (thisTrip.getSourceClosed() && thisTrip.getDestinationClosed()) {
                    thisArrival.setArrivalTime("CLOSED");
                    thisArrival.setTravelTime("CLOSED");
                } else if (thisTrip.getSourceClosed()) {
                    thisArrival.setArrivalTime("CLOSED");
                    thisArrival.setTravelTime("CLOSED");
                } else if (thisTrip.getDestinationClosed()) {
                    Date _24HourDt = _24HourSDF.parse(thisTrip.getArrivalTime());
                    assert _24HourDt != null;
                    thisArrival.setArrivalTime(_12HourSDF.format(_24HourDt));
                    thisArrival.setTravelTime("CLOSED");
                } else {
                    if (Objects.equals(thisTrip.getArrivalTime(), "")) {
                        thisArrival.setArrivalTime("CLOSED");
                        thisArrival.setTravelTime("CLOSED");
                    } else {
                        Date _24HourDtArrival = _24HourSDF.parse(thisTrip.getArrivalTime());
                        assert _24HourDtArrival != null;
                        Date _24HourDtTravel = new Date(_24HourDtArrival.getTime() + (travelTime * MILLISECONDS));
                        thisArrival = new Arrival(_12HourSDF.format(_24HourDtArrival), _12HourSDF.format(_24HourDtTravel));
                    }
                }
                arrivals.add(i, thisArrival);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return arrivals;
    }
}

