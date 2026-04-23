package com.smartcampus.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    public Map<String, Room> getRooms() { return rooms; }
    public Room getRoom(String id) { return rooms.get(id); }
    public void addRoom(Room room) { rooms.put(room.getId(), room); }
    public boolean deleteRoom(String id) { return rooms.remove(id) != null; }

    public Map<String, Sensor> getSensors() { return sensors; }
    public Sensor getSensor(String id) { return sensors.get(id); }
    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        readings.putIfAbsent(sensor.getId(), Collections.synchronizedList(new ArrayList<>()));
    }
    public boolean deleteSensor(String id) {
        readings.remove(id);
        return sensors.remove(id) != null;
    }

    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, Collections.emptyList());
    }
    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>())).add(reading);
    }

    private void seedData() {
        Room r1 = new Room("LIB-301", "Library Quiet Study", 40);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        Room r3 = new Room("HALL-A", "Main Lecture Hall A", 200);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "LAB-101");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "OFFLINE", 19.1, "HALL-A");

        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);
        sensors.put(s4.getId(), s4);

        readings.put(s1.getId(), Collections.synchronizedList(new ArrayList<>()));
        readings.put(s2.getId(), Collections.synchronizedList(new ArrayList<>()));
        readings.put(s3.getId(), Collections.synchronizedList(new ArrayList<>()));
        readings.put(s4.getId(), Collections.synchronizedList(new ArrayList<>()));

        Room room1 = rooms.get(s1.getRoomId());
        if (room1 != null) room1.getSensorIds().add(s1.getId());
        Room room2 = rooms.get(s2.getRoomId());
        if (room2 != null) room2.getSensorIds().add(s2.getId());
        Room room3 = rooms.get(s3.getRoomId());
        if (room3 != null) room3.getSensorIds().add(s3.getId());
        Room room4 = rooms.get(s4.getRoomId());
        if (room4 != null) room4.getSensorIds().add(s4.getId());

        readings.get("TEMP-001").add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis() - 60000, 21.8));
        readings.get("TEMP-001").add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 22.5));
        readings.get("CO2-001").add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 412.0));
    }
}
