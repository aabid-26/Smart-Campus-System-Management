package com.management.smartcampusapi.model;

import java.util.UUID;

public class SensorReading {

    private String id; // unique identifer for the event id
    private long timestamp;
    private double value;

    // The framework calls this constructor when it receives JSON
    public SensorReading() {
        // The ID and Timestamp are automatically generated
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}