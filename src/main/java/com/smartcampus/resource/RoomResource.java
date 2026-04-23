package com.smartcampus.resource;

import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        return Response.ok(roomList).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().trim().isEmpty()) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Room id is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (store.getRoom(room.getId()) != null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "A room with id '" + room.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }
        store.addRoom(room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) throw new ResourceNotFoundException("Room not found: " + roomId);
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) throw new ResourceNotFoundException("Room not found: " + roomId);
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room '" + roomId + "' still has " + room.getSensorIds().size() + " sensor(s). Remove them first.");
        }
        store.deleteRoom(roomId);
        return Response.noContent().build();
    }
}
