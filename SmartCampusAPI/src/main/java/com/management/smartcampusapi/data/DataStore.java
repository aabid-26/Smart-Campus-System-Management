package com.management.smartcampusapi.data;

import com.management.smartcampusapi.model.Room;
import com.management.smartcampusapi.model.Sensor;
import com.management.smartcampusapi.model.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    //This is for rooms storage
    public static Map<String, Room> rooms = new ConcurrentHashMap<>();
    
    //This is for sensors storage 
    public static Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    
    //This is for readings storage
    public static Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

}