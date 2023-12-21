package com.davidp799.patcotoday.utils;

/** Object containing arrival time and source and destination station status.*/
public class Trip {
    private String arrivalTime;
    private boolean sourceClosed;
    private boolean destinationClosed;

    public Trip(String arrivalTime, Boolean sourceClosed, Boolean destinationClosed) {
        this.arrivalTime = arrivalTime;
        this.sourceClosed = sourceClosed;
        this.destinationClosed = destinationClosed;
    }
    public String getArrivalTime() {
        return arrivalTime;
    }
    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    public boolean getSourceClosed() {
        return sourceClosed;
    }
    public void setSourceClosed(boolean sourceClosed) {
        this.sourceClosed = sourceClosed;
    }
    public boolean getDestinationClosed() {
        return destinationClosed;
    }
    public void setDestinationClosed(boolean destinationClosed) {
        this.destinationClosed = destinationClosed;
    }
}
