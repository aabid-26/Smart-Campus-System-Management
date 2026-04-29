package com.management.smartcampusapi.exceptions;

public class SensorUnavailableException extends RuntimeException {

    private final String sensorID;
    private final String status;

    public SensorUnavailableException(String sensorId, String status) {
        super("Sensor '" + sensorId + "' cannot accept readings. Current status: " + status);
        this.sensorID = sensorId;
        this.status = status;
    }

    public String getSensorID() { return sensorID; }
    public String getStatus() { return status; }
}