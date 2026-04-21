package com.management.smartcampusapi.resources;

import com.management.smartcampusapi.data.DataStore;
import com.management.smartcampusapi.exceptions.RoomNotEmptyException;
import com.management.smartcampusapi.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // GET /api/v1/rooms
    @GET
    public Response getAllRooms() {
        List<Room> allRooms = new ArrayList<>(DataStore.rooms.values());
        return Response.ok(allRooms).build();
    }

    // POST /api/v1/rooms
    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        
        // 1. Validation (Simplified to one line each using our helper method!)
        if (room == null || room.getId() == null || room.getId().trim().isEmpty()) {
            return buildError(Response.Status.BAD_REQUEST, "Room 'id' is required.");
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return buildError(Response.Status.BAD_REQUEST, "Room 'name' is required.");
        }
        if (DataStore.rooms.containsKey(room.getId())) {
            return buildError(Response.Status.CONFLICT, "A room with id '" + room.getId() + "' already exists.");
        }

        // 2. Setup and Save
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        DataStore.rooms.put(room.getId(), room);

        // 3. Build Response
        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Room created successfully.");
        response.put("room", room);

        return Response.created(location).entity(response).build();
    }

    // GET /api/v1/rooms/{roomId}
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);

        if (room == null) {
            return buildError(Response.Status.NOT_FOUND, "Room '" + roomId + "' does not exist.");
        }

        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId}
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);

        if (room == null) {
            return buildError(Response.Status.NOT_FOUND, "Room '" + roomId + "' does not exist or has already been deleted.");
        }

        // Coursework Part 2: Safety logic constraint
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }

        DataStore.rooms.remove(roomId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Room '" + roomId + "' has been successfully decommissioned.");
        response.put("deletedRoomId", roomId);

        return Response.ok(response).build();
    }

    // ------------------------------------------------------------------
    // HELPER METHOD: This handles building the error JSON automatically!
    // ------------------------------------------------------------------
    private Response buildError(Response.Status status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status.getStatusCode());
        error.put("error", status.getReasonPhrase()); // Automatically gets "Bad Request", "Not Found", etc.
        error.put("message", message);
        
        return Response.status(status).entity(error).build();
    }
}