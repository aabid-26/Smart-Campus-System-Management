package com.management.smartcampusapi.data;

import com.management.smartcampusapi.model.Room;
import com.management.smartcampusapi.model.Sensor;
import com.management.smartcampusapi.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    // Key: roomId -> Room object
    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Key: sensorId -> Sensor object
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Key: sensorId -> List of SensorReading objects
    public static final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    static {
        // --- Pre-load Rooms ---
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        Room r3 = new Room("HALL-01", "Main Lecture Hall", 200);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // --- Pre-load Sensors ---
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",    22.5,  "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE",    412.0, "LAB-101");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "ACTIVE",    18.0,  "HALL-01");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "MAINTENANCE", 0.0, "LAB-101");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);
        sensors.put(s4.getId(), s4);

        // --- Link Sensors to Rooms ---
        r1.getSensorIds().add(s1.getId());
        r2.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s4.getId());
        r3.getSensorIds().add(s3.getId());

        // --- Pre-load some Readings for TEMP-001 ---
        List<SensorReading> readings1 = new ArrayList<>();
        readings1.add(new SensorReading("READ-0001", 1745000000000L, 21.0));
        readings1.add(new SensorReading("READ-0002", 1745003600000L, 21.8));
        readings1.add(new SensorReading("READ-0003", 1745007200000L, 22.5));
        sensorReadings.put(s1.getId(), readings1);

        // --- Pre-load some Readings for CO2-001 ---
        List<SensorReading> readings2 = new ArrayList<>();
        readings2.add(new SensorReading("READ-0004", 1745000000000L, 400.0));
        readings2.add(new SensorReading("READ-0005", 1745003600000L, 408.0));
        readings2.add(new SensorReading("READ-0006", 1745007200000L, 412.0));
        sensorReadings.put(s2.getId(), readings2);

        // --- Empty reading lists for remaining sensors ---
        sensorReadings.put(s3.getId(), new ArrayList<>());
        sensorReadings.put(s4.getId(), new ArrayList<>());
    }
}