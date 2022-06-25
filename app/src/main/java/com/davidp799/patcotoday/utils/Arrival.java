package com.davidp799.patcotoday.utils;

/** Object containing arrival time and travel time for each scheduled arrival.
 */

public class Arrival{
    private String arrivalTime;
    private String travelTime;

    public Arrival(String arrivalTime, String travelTime) {
        this.arrivalTime = arrivalTime;
        this.travelTime = travelTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(String travelTime) {
        this.travelTime = travelTime;
    }
}
