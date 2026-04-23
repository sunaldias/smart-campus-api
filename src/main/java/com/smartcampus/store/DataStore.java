package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    static {
        Room sampleRoom = new Room("LIB-301", "Library Quiet Study", 40);
        rooms.put(sampleRoom.getId(), sampleRoom);

        Sensor sampleSensor = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        sensors.put(sampleSensor.getId(), sampleSensor);
        sampleRoom.getSensorIds().add(sampleSensor.getId());

        List<SensorReading> sampleReadings = Collections.synchronizedList(new ArrayList<>());
        sampleReadings.add(new SensorReading("R1", System.currentTimeMillis(), 22.5));
        readings.put(sampleSensor.getId(), sampleReadings);
    }

    private DataStore() {
    }
}