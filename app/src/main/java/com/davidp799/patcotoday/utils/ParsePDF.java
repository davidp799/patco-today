package com.davidp799.patcotoday.utils;

import java.util.ArrayList;
import java.util.Collections;

public class ParsePDF {
    private final String[] pdfLines;
    public ParsePDF(String pdfText) {
        this.pdfLines = pdfText.split("\n", 1024);
    }
    /** Function which adds arrival time lines by finding start and end point
     *  and converts into indexed list of arrival times for each source station.
     *  @return list of strings representing travel times */
    public ArrayList<ArrayList<Trip>> getArrivalLines(int source_id, int destination_id) {
        ArrayList<ArrayList<Trip>> allFormattedArrivals = new ArrayList<>();
        ArrayList<Trip> finalEastbound = new ArrayList<>();
        ArrayList<Trip> finalWestbound = new ArrayList<>();

        for (String pdfLine : pdfLines) {
            String newString = pdfLine;
            String[] badRegexValues = {"\\s", "à", "A", "P"};
            String[] replacementValues = {"", "CLOSED,", "A,", "P,"};
            for (int i=0; i<badRegexValues.length; i++) {
                newString = newString.replaceAll(badRegexValues[i], replacementValues[i]);
            }
            String[] unformattedTrips = newString.split(",");
            ArrayList<Trip> formattedTrips = new ArrayList<>(); // all w & e

            if (unformattedTrips.length >= 13) {
                for (String unformattedTrip : unformattedTrips) {
                    Trip formattedTrip = new Trip("", false, false);
                    if (unformattedTrip.contains("A") && unformattedTrip.contains("12:")) {
                        String arrivalTime = unformattedTrip.replace("12:", "00:");
                        arrivalTime = arrivalTime.replace("A", "");
                        formattedTrip.setArrivalTime(arrivalTime);
                    } else if (unformattedTrip.contains("A")) {
                        String arrivalTime = unformattedTrip.replace("A", "");
                        formattedTrip.setArrivalTime(arrivalTime);
                    } else if (unformattedTrip.contains("P") && unformattedTrip.contains("12:")) {
                        String arrivalTime = unformattedTrip.replace("P", "");
                        formattedTrip.setArrivalTime(arrivalTime);
                    } else if (unformattedTrip.contains("P") && unformattedTrip.contains("11:")) {
                        String arrivalTime = unformattedTrip.replace("11:", "23:");
                        arrivalTime = arrivalTime.replace("P", "");
                        formattedTrip.setArrivalTime(arrivalTime);
                    } else if (unformattedTrip.contains("P")) {
                        String[] splitSubString = unformattedTrip.split(":");
                        int hour = Integer.parseInt(splitSubString[0]);
                        String arrivalTime;
                        if (hour == 11) {
                            arrivalTime = (hour) + ":" + splitSubString[1];
                        } else {
                            arrivalTime = (hour + 12) + ":" + splitSubString[1];
                        }
                        arrivalTime = arrivalTime.replace("P", "");
                        formattedTrip.setArrivalTime(arrivalTime);
                    }
                    // Set source and destination station status for trip
                    if (unformattedTrips[source_id].contains("CLOSED")) {
                        formattedTrip.setSourceClosed(true);
                    }
                    if (unformattedTrips[destination_id].contains("CLOSED")) {
                        formattedTrip.setDestinationClosed(true);
                    }
                    formattedTrips.add(formattedTrip);
                }

                if (formattedTrips.size() >= 13) {
                    ArrayList<Trip> westboundArrayList = new ArrayList<>(formattedTrips.subList(0,13));
                    finalWestbound.addAll(westboundArrayList);
                    ArrayList<Trip> eastboundArrayList = new ArrayList<>(formattedTrips.subList(13, formattedTrips.size()));
                    Collections.reverse(eastboundArrayList);
                    finalEastbound.addAll(eastboundArrayList);
                }

            }
        }
        allFormattedArrivals.add(finalWestbound);
        allFormattedArrivals.add(finalEastbound);
        return allFormattedArrivals;
    }
}
