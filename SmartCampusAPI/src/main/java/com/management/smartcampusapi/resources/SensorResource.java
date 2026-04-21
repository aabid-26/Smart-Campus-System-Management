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
import java.util.concurrent.CopyOnWriteArrayList; // Upgraded for Thread Safety
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET /api/v1/sensors?type=CO2
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
    @POST
    public Response createSensor(Sensor sensor) {
        // 1. Validation (Simplified with helper method!)
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return buildError(Response.Status.BAD_REQUEST, "Sensor 'id' is required.");
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            return buildError(Response.Status.BAD_REQUEST, "Sensor 'type' is required.");
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return buildError(Response.Status.BAD_REQUEST, "Sensor 'roomId' is required.");
        }

        // Check the referenced room actually exists (Part 3 Validation)
        if (!DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("roomId", sensor.getRoomId());
        }

        if (DataStore.sensors.containsKey(sensor.getId())) {
            return buildError(Response.Status.CONFLICT, "A sensor with id '" + sensor.getId() + "' already exists.");
        }

        // 2. Setup and Save
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        DataStore.sensors.put(sensor.getId(), sensor);
        DataStore.rooms.get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // CRITICAL FIX: Upgraded to thread-safe CopyOnWriteArrayList
        DataStore.sensorReadings.put(sensor.getId(), new CopyOnWriteArrayList<>());

        // 3. Build Response
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
            return buildError(Response.Status.NOT_FOUND, "Sensor '" + sensorId + "' does not exist.");
        }

        return Response.ok(sensor).build();
    }

    // DELETE /api/v1/sensors/{sensorId}
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            return buildError(Response.Status.NOT_FOUND, "Sensor '" + sensorId + "' does not exist.");
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
    // Part 4 - Sub-Resource Locator (The "Traffic Cop")
    // -------------------------------------------------------------------------
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    // HELPER METHOD: Builds JSON error responses automatically
    private Response buildError(Response.Status status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status.getStatusCode());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        
        return Response.status(status).entity(error).build();
    }
}