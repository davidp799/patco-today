package com.davidp799.patcotoday.utils;

import android.util.Log;

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
    public ArrayList<ArrayList<String>> getArrivalLines() {
        ArrayList<ArrayList<String>> allFormattedArrivals = new ArrayList<>();
        ArrayList<String> finalEastbound = new ArrayList<>();
        ArrayList<String> finalWestbound = new ArrayList<>();

        for (String pdfLine : pdfLines) {
            String newString = pdfLine;
            String[] badRegexValues = {"\\s", "à", "A", "P"};
            String[] replacementValues = {"", "99:99A", "A,", "P,"};
            for (int i=0; i<badRegexValues.length; i++) {
                newString = newString.replaceAll(badRegexValues[i], replacementValues[i]);
            }
            String[] stringsList = newString.split(",");
            ArrayList<String> formattedStringsList = new ArrayList<>(); // all w & e

            if (stringsList.length >= 13) {
                for (String subString : stringsList) {
                    if (subString.contains("A") && subString.contains("12:")) {
                        String formattedSubString = subString.replace("12:", "00:");
                        formattedSubString = formattedSubString.replace("A", "");
                        formattedStringsList.add(formattedSubString);
                    } else if (subString.contains("A")) {
                        String formattedSubString = subString.replace("A", "");
                        formattedStringsList.add(formattedSubString);
                    } else if (subString.contains("P") && subString.contains("12:")) {
                        String formattedSubString = subString.replace("P", "");
                        formattedStringsList.add(formattedSubString);
                    } else if (subString.contains("P") && subString.contains("11:")) {
                        String formattedSubString = subString.replace("11:", "23:");
                        formattedSubString = formattedSubString.replace("P", "");
                        formattedStringsList.add(formattedSubString);
                    } else if (subString.contains("P")) {
                        String[] splitSubString = subString.split(":");
                        int hour = Integer.parseInt(splitSubString[0]);
                        String formattedSubString;
                        if (hour == 11) {
                            formattedSubString = (hour) + ":" + splitSubString[1];
                        } else {
                            formattedSubString = (hour + 12) + ":" + splitSubString[1];
                        }
                        formattedSubString = formattedSubString.replace("P", "");
                        formattedStringsList.add(formattedSubString);
                    }
                }

                if (formattedStringsList.size() >= 13) {
                    ArrayList<String> westboundArrayList = new ArrayList<>(formattedStringsList.subList(0,13));
                    finalWestbound.addAll(westboundArrayList);
                    ArrayList<String> eastboundArrayList = new ArrayList<>(formattedStringsList.subList(13, formattedStringsList.size()));
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
