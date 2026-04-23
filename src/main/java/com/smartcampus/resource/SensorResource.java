package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> list = new ArrayList<>();
        for (Sensor s : store.getSensors().values()) {
            if (type == null || s.getType().equalsIgnoreCase(type)) {
                list.add(s);
            }
        }
        return Response.ok(list).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Sensor id is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (store.getSensor(sensor.getId()) != null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Sensor '" + sensor.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "roomId is required.");
            return Response.status(422).entity(err).build();
        }
        Room room = store.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room '" + sensor.getRoomId() + "' does not exist.");
        }
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }
        store.addSensor(sensor);
        room.getSensorIds().add(sensor.getId());
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) throw new ResourceNotFoundException("Sensor not found: " + sensorId);
        return Response.ok(sensor).build();
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) throw new ResourceNotFoundException("Sensor not found: " + sensorId);
        Room room = store.getRoom(sensor.getRoomId());
        if (room != null) room.getSensorIds().remove(sensorId);
        store.deleteSensor(sensorId);
        return Response.noContent().build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) throw new ResourceNotFoundException("Sensor not found: " + sensorId);
        return new SensorReadingResource(sensor);
    }
}
