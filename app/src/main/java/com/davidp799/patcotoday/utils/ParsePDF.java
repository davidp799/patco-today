package com.davidp799.patcotoday.utils;

import java.sql.SQLOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ParsePDF {
    private final String[] pdfLines;
    private final ArrayList<String> specialFromToTimes;
    private final char[] badChars = "BCDEFGHIJKLNOQRSTUVWXYZ".toCharArray();

    public ParsePDF(String pdfText, ArrayList<String> specialFromToTimes) {
        this.pdfLines = pdfText.split("\n", 1024);
        this.specialFromToTimes = specialFromToTimes;
    }
    /** Function which adds arrival time lines by finding start and end point
     *  and converts into indexed list of arrival times for each source station.
     *  @return list of strings representing travel times */
    public ArrayList<ArrayList<String>> getArrivalLines() {
        /* Initialize return list of arrays */
        ArrayList<ArrayList<String>> allArrivals = new ArrayList<>(); // both lists
        /* Initialize organizer arrays */
        ArrayList<String> eastbound = new ArrayList<>(); // routeid = 1
        ArrayList<String> westbound = new ArrayList<>(); // routeid = 2
        /* Set collect line status to 0 [do not collect] */
        int collect = 0;
        /* Search for lines containing special arrivals */
        for (int i=0; i<pdfLines.length; i++) {
            /* Parse only lines containing special arrivals; only parse when collect == 1 */
            if (specialFromToTimes.size() > 1) {
                if (pdfLines[i].contains(specialFromToTimes.get(0))) {
                    collect = 1; // ready to parse special arrivals in following line
                } else if (pdfLines[i].contains(specialFromToTimes.get(1))) { // TRAIN RETURNS TO NORMAL SCHEDULE. REFER TO TIMETABLE FOR DEPARTURE TIMES.
                    collect = 2; // finish parsing special arrivals at this line
                } else if (collect == 1) { // parse current line for special arrivals
                    /* Strip white-space from string */
                    String str = pdfLines[i];
                    str = str.replaceAll("\\s","");
                    str = str.replaceAll("à", "99:99A");
                    /* Split string into array of AM arrival times and remove blanks */
//                    System.out.println("@@@ CURRENT STRING = " + str);
                    String[] splitAM = str.split("[A]", 512); // split line at A (not P yet)
//                    System.out.println("@@@ SPLIT STRING = " + Arrays.toString(splitAM));
                    ArrayList<String> allTimes = new ArrayList<>(); // split line at A (not P yet)
                    for (String s : splitAM) {
                        if (s.length() > 0) {
                            /* Check for un-parsed PM times */
                            if (s.contains("P")) { // check for incomplete "PM" times
                                /* Split strings into array of PM arrival times and remove blanks */
//                                System.out.println("@@@ CURRENT PM STRING = " + str);
                                String[] splitPM = s.split("P", 512); // split line at P
//                                System.out.println("@@@ SPLIT PM STRING = " + Arrays.toString(splitPM));
                                for (String t : splitPM) {
                                    if (t.length() > 0) {
                                        /* Change PM times to 24hr format */
                                        if (!t.contains("12:")) { // add 12 to all PM hours except 12pm
                                            String[] split = t.split(":");
                                            int hour = Integer.parseInt(split[0]);
                                            String arrivalTime;
                                            if (hour == 11) {
                                                arrivalTime = (hour) + ":" + split[1];
                                            } else {
                                                arrivalTime = (hour + 12) + ":" + split[1];
                                            }
                                            allTimes.add(arrivalTime);
                                        } else {
                                            allTimes.add(t);
                                        }
                                    }
                                }
                            } else if (s.contains("12:") && !s.contains("P")) {
                                /* Change AM times to 24hr format */
                                String arrivalTime = s.replace("12:", "00:");
                                allTimes.add(arrivalTime);
                            } else {
                                allTimes.add(s);
                            }
                        }
                    }
                    /* Split allTimes into Westbound and Eastbound arrivals */
                    int isWestbound = 0; // assume current time is westbound [since LtR is WB->EB]
                    for (int j=0; j<allTimes.size(); j++) { // iterate through all arrival times
                        /* Add current time to proper array, based on previously determined isWestbound boolean value */
                        if (isWestbound > 0) {
                            eastbound.add(allTimes.get(j));
                        } else {
                            westbound.add(allTimes.get(j));
                        }
                        /* Check if next time is less than current time */
                        if (j < allTimes.size()-1) { // do not check last arrival time or PM times
                            String currentTime = allTimes.get(j);
                            String nextTime = allTimes.get(j + 1);
                            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm", Locale.US);
                            /* Check if next time is less than current time [switch from wb to eb] */
                            try {
                                Date current24HourDt = _24HourSDF.parse(currentTime);
                                if (nextTime.equals("99:99")) {
                                    isWestbound += 0;
                                    System.out.println("$$$ OOPS 99:99 FOUND");
                                } else {
                                    Date next24HourDt = _24HourSDF.parse(nextTime);
                                    System.out.println("%%% next24HourDt = " + next24HourDt);
                                    assert next24HourDt != null;
                                    if (next24HourDt.before(current24HourDt)) {
                                        isWestbound += 1;
                                    } else {
                                        isWestbound += 0;
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                                System.out.println("$$$ ERROR CHECKING TIMES");
                            }
                        }
                    }
                }
            }
            else {
                // parse current line for special arrivals
                /* Strip white-space from string */
                String str = pdfLines[i];
                str = str.replaceAll("\\s","");
                str = str.replaceAll("à", "99:99A");
                /* Split string into array of AM arrival times and remove blanks */
//                System.out.println("@@@ CURRENT STRING = " + str);
                String[] splitAM = str.split("[A]", 512); // split line at A (not P yet)
//                System.out.println("@@@ SPLIT STRING = " + Arrays.toString(splitAM));
                ArrayList<String> allTimes = new ArrayList<>(); // split line at A (not P yet)
                for (String s : splitAM) {
                    if (s.length() > 0 && !s.matches(".*[BCDEFGHIJKLNOQRSTUVWXYZbcdefghijklnoqrstuvwxyz,.].*")) {
                        /* Check for un-parsed PM times */
                        if (s.contains("P")) { // check for incomplete "PM" times
                            /* Split strings into array of PM arrival times and remove blanks */
                            String[] splitPM = s.split("P", 512); // split line at P
                            for (String t : splitPM) {
                                if(t.length() > 0 && !t.matches(".*[BCDEFGHIJKLNOQRSTUVWXYZbcdefghijklnoqrstuvwxyz,.].*")) {
                                    /* Change PM times to 24hr format */
                                    if (!t.contains("12:")) { // add 12 to all PM hours except 12pm
                                        String[] split = t.split(":");
//                                        System.out.println("$$$$$$$$" + split[0]);
                                        int hour = Integer.parseInt(split[0]);
                                        String arrivalTime;
                                        if (hour == 11) {
                                            arrivalTime = (hour) + ":" + split[1];
                                        } else {
                                            arrivalTime = (hour + 12) + ":" + split[1];
                                        }
                                        allTimes.add(arrivalTime);
                                    } else {
                                        allTimes.add(t);
                                    }
                                }
                            }
                        } else if (s.contains("12:") && !s.contains("P")) {
                            /* Change AM times to 24hr format */
                            String arrivalTime = s.replace("12:", "00:");
                            allTimes.add(arrivalTime);
                        } else {
                            allTimes.add(s);
                        }
                    }
                }
                /* Split allTimes into Westbound and Eastbound arrivals */
                int isWestbound = 0; // assume current time is westbound [since LtR is WB->EB]
                System.out.println("$$$ alltimes.size = " + allTimes.size());
                System.out.println(allTimes.size() / 2);
                for (int j=0; j<allTimes.size(); j++) { // iterate through all arrival times
//                    System.out.println("@@@ ALL TIMES = " + allTimes.get(j));
                    /* Add current time to proper array, based on previously determined isWestbound boolean value */
                    if (isWestbound > 0) {
                        eastbound.add(allTimes.get(j));
                    } else {
                        westbound.add(allTimes.get(j));
                    }
                    /* Check if next time is less than current time */
                    if (j < allTimes.size()-1) { // do not check last arrival time or PM times
                        String currentTime = allTimes.get(j);
                        String nextTime = allTimes.get(j + 1);
                        SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm", Locale.US);
                        /* Check if next time is less than current time [switch from wb to eb] */
                        try {
                            Date current24HourDt = _24HourSDF.parse(currentTime);
                            if (nextTime.equals("99:99")) {
                                isWestbound += 0;
                                System.out.println("$$$ OOPS 99:99 FOUND: parsed as = " + _24HourSDF.parse(nextTime));
                                allTimes.set(j+1, allTimes.get(j));
                            } else {
                                Date next24HourDt = _24HourSDF.parse(nextTime);
                                assert next24HourDt != null;
                                if (next24HourDt.before(current24HourDt)) {
                                    isWestbound += 1;
                                } else {
                                    isWestbound += 0;
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            System.out.println("$$$ ERROR CHECKING TIMES");
                        }
                    }
                }
            }
        }
        System.out.println("@@@ WESTBOUND TIMES = " + westbound);
        System.out.println("@@@ EASTBOUND TIMES = " + eastbound);
        allArrivals.add(westbound);
        allArrivals.add(eastbound);
        return allArrivals;
    }
}