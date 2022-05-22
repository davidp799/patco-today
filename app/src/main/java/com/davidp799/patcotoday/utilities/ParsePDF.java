package com.davidp799.patcotoday.utilities;

import java.util.ArrayList;

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
            }
        }
        return 2;
    }
    /** Function which adds arrival time lines by finding start and end point
     *  and converts into indexed list of arrival times for each source station.
     *  @return list of strings representing travel times */
    public ArrayList<String> getArrivalLines() {
        ArrayList<String> arrivals = new ArrayList<>();

        int collect = 0;
        for (int i=0; i< pdfLines.length; i++) { // search for lines containing travel times
            if (pdfLines[i].equals("SPECIAL SCHEDULE STARTS WITH ADJUSTED DEPARTURE TIMES BELOW.")) {
                collect = 1;
            } else if (pdfLines[i].equals("TRAIN RETURNS TO NORMAL SCHEDULE. REFER TO TIMETABLE FOR DEPARTURE TIMES.")) {
                collect = 2;
            } else if (collect == 1) { // collect if current line contains arrivals
                // remove whitespace
                String str = pdfLines[i];
                str = str.replaceAll("\\s","");
                // find AM arrivals
                String[] timesAM = str.split("A", 1024); // split line at A (not P yet)
                for (int j=0; j<timesAM.length; j++) { // iterate through all "AM" times
                    if (timesAM[j].length() > 0) { // skip blank lines
                        if (timesAM[j].contains("P")) { // check for incomplete "PM" times
                            String[] timesPM = timesAM[j].split("P", 1024); // split line at P
                            for (int k=0; k<timesPM.length; k++) { // iterate through all "PM" times
                                if (timesPM[k].length() > 0) { // skip blank lines
                                    /* Change PM times to 24hr format */
                                    if (!timesPM[k].contains("12:")) { // add 12 to all PM hours except 12pm
                                        String[] split = timesPM[k].split(":");
                                        int hour = Integer.parseInt(split[0]);
                                        String arrivalTime = (hour + 12) + ":" + split[1];
                                        arrivals.add(arrivalTime);
                                    } else { // add 12pm hour
                                        String arrivalTime = timesPM[k];
                                        arrivals.add(arrivalTime);
                                    }
                                }
                            }
                        }
                        /* Change AM times to 24hr format */
                        else if (timesAM[j].contains("12:")) { // change 12:00am to 00:00
                            String arrivalTime = timesAM[j].replace("12:", "00:");
                            arrivals.add(arrivalTime);
                        } else { // add rest of AM 24hr times
                            String arrivalTime = timesAM[j];
                            arrivals.add(arrivalTime);
                        }
                    }
                }
            }
        }
        return arrivals;
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