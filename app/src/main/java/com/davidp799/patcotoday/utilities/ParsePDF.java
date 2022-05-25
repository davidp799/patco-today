package com.davidp799.patcotoday.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ParsePDF {
    private String pdfText;
    private final String[] pdfLines;

    public ParsePDF(String pdfText) {
        setPdfText(pdfText);
        pdfLines = pdfText.split("\n", 1024);
    }
    public int getRouteID() {
        // split pdfText into list of pdfLines
        // if direction line contains EASTBOUND, return 1; otherwise return 2
        for (String line : pdfLines) {
            if (line.equals("EASTBOUND")) {
                return 1;
            } else if (line.equals("WESTBOUND")) {
                return 2;
            }
        }
        return 3;
    }
    /** Function which adds arrival time lines by finding start and end point
     *  and converts into indexed list of arrival times for each source station.
     *  @return list of strings representing travel times */
    public ArrayList<ArrayList<String>> getArrivalLines(int routeID) {
        ArrayList<String> eastbound = new ArrayList<>(); // routeid = 1
        ArrayList<String> westbound = new ArrayList<>(); // routeid = 2
        ArrayList<ArrayList<String>> allArrivals = new ArrayList<>(); // both lists

        int collect = 0;
        for (int i=0; i< pdfLines.length; i++) { // search for lines containing travel times

            if (pdfLines[i].contains("SPECIAL SCHEDULE STARTS WITH ADJUSTED DEPARTURE TIMES BELOW.")) {
                collect = 1;
            } else if (pdfLines[i].contains("SCHEDULE")) { // TRAIN RETURNS TO NORMAL SCHEDULE. REFER TO TIMETABLE FOR DEPARTURE TIMES.
                collect = 2;
            } else if (collect == 1) { // collect if current line contains arrivals
                /* Strip white-space from string */
                String str = pdfLines[i];
                str = str.replaceAll("\\s","");
                /* Split string into array of AM arrival times and remove blanks */
                String[] splitAM = str.split("A", 512); // split line at A (not P yet)
                ArrayList<String> timesAM = new ArrayList<>(); // split line at A (not P yet)
                for (String s : splitAM) {
                    if (s.length() > 0) {
                        timesAM.add(s);
                    }
                }
                /* Split AM times into Westbound and Eastbound arrivals */
                int isWestbound = 0; // assume current time is westbound
                for (int j=0; j<timesAM.size(); j++) { // iterate through all "AM" times
                    /* Change AM times to 24hr format */
                    if (timesAM.get(j).contains("12:") && !timesAM.get(j).contains("P")) { // change 12:00am to 00:00
                        String arrivalTime = timesAM.get(j).replace("12:", "00:");
                        timesAM.set(j, arrivalTime);
                    }
                    /* Add current time to proper array, based on previously determined isWestbound boolean value */
                    if (isWestbound > 0) {
                        eastbound.add(timesAM.get(j));
                        System.out.println(timesAM.get(j));
                    } else {
                        westbound.add(timesAM.get(j));
                        System.out.println(timesAM.get(j));
                    }
                    /* Check if next time is less than current time */
                    if (j < timesAM.size()-1 && !timesAM.get(j).contains("P")) { // do not check last arrival time or PM times
                        String currentTime = timesAM.get(j);
                        String nextTime = timesAM.get(j + 1);
                        SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm", Locale.US);
                        /* Check if next time is less than current time [switch from wb to eb] */
                        try {
                            Date current24HourDt = _24HourSDF.parse(currentTime);
                            Date next24HourDt = _24HourSDF.parse(nextTime);
                            if (next24HourDt.before(current24HourDt)) {
                                isWestbound += 1;
                            } else {
                                isWestbound += 0;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    /* Check for un-parsed PM times */
                    if (timesAM.get(j).contains("P")) { // check for incomplete "PM" times
                        /* Split strings into array of PM arrival times and remove blanks */
                        String[] splitPM = timesAM.get(j).split("P", 512); // split line at P
                        ArrayList<String> timesPM = new ArrayList<>(); // split line at A (not P yet)
                        for (String s : splitPM) {
                            if (s.length() > 0) {
                                timesPM.add(s);
                            }
                        }
                        int pmWestbound = 0; // assume current time is westbound
                        for (int k=0; k<timesPM.size(); k++) { // iterate through all "PM" times
                            /* Change PM times to 24hr format */
                            if (!timesPM.get(k).contains("12:")) { // add 12 to all PM hours except 12pm
                                String[] split = timesPM.get(k).split(":");
                                int hour = Integer.parseInt(split[0]);
                                String arrivalTime = (hour + 12) + ":" + split[1];
                                timesPM.set(k, arrivalTime);
                            }
                            /* Add current time to proper array, based on previously determined isWestbound boolean value */
                            if (pmWestbound > 0) {
                                eastbound.add(timesPM.get(k));
                                System.out.println(timesPM.get(k));
                            } else {
                                westbound.add(timesPM.get(k));
                                System.out.println(timesPM.get(k));
                            }
                            /* Check if next time is less than current time */
                            if (k < timesPM.size()-1) { // do not check last arrival time
                                String currentTime = timesPM.get(k);
                                String nextTime = timesPM.get(k + 1);
                                SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm", Locale.US);
                                /* Check if next time is less than current time [switch from wb to eb] */
                                try {
                                    Date current24HourDt = _24HourSDF.parse(currentTime);
                                    Date next24HourDt = _24HourSDF.parse(nextTime);
                                    if (next24HourDt.before(current24HourDt)) {
                                        pmWestbound += 1;
                                    } else {
                                        pmWestbound += 0;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                }
            }
        }
        if (routeID == 1) {
            allArrivals.add(eastbound);
        } else if (routeID == 2) {
            allArrivals.add(westbound);
        } else {
            allArrivals.add(westbound);
            allArrivals.add(eastbound);
        }
        return allArrivals;
    }
    /** Accessor for pdf text
     * @return pdfText text value of pdf */
    public String getPdfText() {
        return pdfText;
    }
    /** Modifier for pdf text
     * @param pdfText string value for input text */
    public void setPdfText(String pdfText) {
        this.pdfText = pdfText;
    }
}