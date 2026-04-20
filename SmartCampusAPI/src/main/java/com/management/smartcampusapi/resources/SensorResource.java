package com.management.smartcampusapi.resources;

import com.management.smartcampusapi.data.DataStore;
import com.management.smartcampusapi.exceptions.LinkedResourceNotFoundException;
import com.management.smartcampusapi.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET /api/v1/sensors
    // Optional query param: ?type=CO2
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(DataStore.sensors.values());

        if (type != null && !type.trim().isEmpty()) {
            result = result.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(result).build();
    }

    // POST /api/v1/sensors
    // Validates that the roomId in the request body actually exists
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("error", "Bad Request");
            error.put("message", "Sensor 'id' is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("error", "Bad Request");
            error.put("message", "Sensor 'type' is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 400);
            error.put("error", "Bad Request");
            error.put("message", "Sensor 'roomId' is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // Check the referenced room actually exists
        if (!DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("roomId", sensor.getRoomId());
        }

        if (DataStore.sensors.containsKey(sensor.getId())) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 409);
            error.put("error", "Conflict");
            error.put("message", "A sensor with id '" + sensor.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        // Save the sensor
        DataStore.sensors.put(sensor.getId(), sensor);

        // Link sensor to the room
        DataStore.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // Initialise an empty readings list for this sensor
        DataStore.sensorReadings.put(sensor.getId(), new ArrayList<>());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sensor created successfully.");
        response.put("sensor", sensor);

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 404);
            error.put("error", "Not Found");
            error.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        return Response.ok(sensor).build();
    }

    // DELETE /api/v1/sensors/{sensorId}
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 404);
            error.put("error", "Not Found");
            error.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        // Remove sensor from its room's sensorIds list
        String roomId = sensor.getRoomId();
        if (roomId != null && DataStore.rooms.containsKey(roomId)) {
            DataStore.rooms.get(roomId).getSensorIds().remove(sensorId);
        }

        // Remove the sensor and its readings
        DataStore.sensors.remove(sensorId);
        DataStore.sensorReadings.remove(sensorId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sensor '" + sensorId + "' has been successfully removed.");
        response.put("deletedSensorId", sensorId);

        return Response.ok(response).build();
    }

    // -------------------------------------------------------------------------
    // Part 4 - Sub-Resource Locator
    // GET/POST /api/v1/sensors/{sensorId}/readings
    //
    // Instead of defining reading paths here, we delegate to
    // SensorReadingResource. Jersey will call this method to get the
    // object that handles the /readings sub-path.
    // -------------------------------------------------------------------------
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
