package com.davidp799.patcotoday;

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
     * @return list of strings representing travel times */
    public ArrayList<Arrival> getArrivalLines() {
        ArrayList<Arrival> arrivals = new ArrayList<>();
        ArrayList<Arrival> result = new ArrayList<>();

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

                for (int j=0; j<timesAM.length; j++) {
                    if (timesAM[j].length() > 0) {
                        if (timesAM[j].contains("P")) {
                            String[] timesPM = timesAM[j].split("P", 1024);
                            for (int k=0; k<timesPM.length; k++) {
                                if (timesPM[k].length() > 0) {
                                    String arrivalTime = timesPM[k] + " PM";
                                    String travelTime = "null";
                                    Arrival arrival = new Arrival(arrivalTime, travelTime);
                                    arrivals.add(arrival);
                                }
                            }
                        } else {
                            String arrivalTime = timesAM[j] + " AM";
                            String travelTime = "null";
                            Arrival arrival = new Arrival(arrivalTime, travelTime);
                            arrivals.add(arrival);
                        }
                    }
                }
            }
        }
        return arrivals;
    }
    /** Function which converts arrivalStrings list into indexed list of arrival times for each source station
     * @param arrivalLines list of arrival lines to parse
     * @return list of lists representing each stations special arrival times */
    public ArrayList<String>[] getArrivals(ArrayList arrivalLines) {
        return null;
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
