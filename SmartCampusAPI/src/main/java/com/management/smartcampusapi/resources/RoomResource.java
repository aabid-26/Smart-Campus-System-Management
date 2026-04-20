package com.management.smartcampusapi.resources;
import com.management.smartcampusapi.data.DataStore;
import com.management.smartcampusapi.exceptions.RoomNotEmptyException;
import com.management.smartcampusapi.model.Room;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
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
        if (room == null || room.getId() == null || room.getId().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("error", "Bad Request");
            error.put("message", "Room 'id' is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        if (room.getName() == null || room.getName().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("error", "Bad Request");
            error.put("message", "Room 'name' is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        if (DataStore.rooms.containsKey(room.getId())) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 409);
            error.put("error", "Conflict");
            error.put("message", "A room with id '" + room.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        DataStore.rooms.put(room.getId(), room);

        // Build the Location header URI pointing to the new room
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();

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
            Map<String, Object> error = new HashMap<>();
            error.put("status", 404);
            error.put("error", "Not Found");
            error.put("message", "Room '" + roomId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId}
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);

        if (room == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 404);
            error.put("error", "Not Found");
            error.put("message", "Room '" + roomId + "' does not exist or has already been deleted.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }

        DataStore.rooms.remove(roomId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Room '" + roomId + "' has been successfully decommissioned.");
        response.put("deletedRoomId", roomId);

        return Response.ok(response).build();
    }
}
