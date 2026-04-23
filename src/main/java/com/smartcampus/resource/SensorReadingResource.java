package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final Sensor sensor;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(Sensor sensor) {
        this.sensor = sensor;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> history = store.getReadings(sensor.getId());
        return Response.ok(history).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor '" + sensor.getId() + "' is under MAINTENANCE.");
        }
        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor '" + sensor.getId() + "' is OFFLINE.");
        }
        if (reading == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Reading body is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        store.addReading(sensor.getId(), reading);
        sensor.setCurrentValue(reading.getValue());
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
