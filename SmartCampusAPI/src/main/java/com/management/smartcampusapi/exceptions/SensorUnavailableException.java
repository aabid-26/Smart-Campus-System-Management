package com.management.smartcampusapi.exceptions;

public class SensorUnavailableException extends RuntimeException {

    private final String sensorID;
    private final String status;

    public SensorUnavailableException(String sensorID, String status) {
        super("Sensor '" + sensorID + "' cannot accept readings. Current status: " + status);
        this.sensorID = sensorID;
        this.status = status;
    }

    public String getSensorID() { return sensorID; }
    public String getStatus() { return status; }
}