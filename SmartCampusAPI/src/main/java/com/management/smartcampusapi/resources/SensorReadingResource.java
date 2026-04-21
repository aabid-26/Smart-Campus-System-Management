package com.management.smartcampusapi.resources;

import com.management.smartcampusapi.data.DataStore;
import com.management.smartcampusapi.exceptions.SensorUnavailableException;
import com.management.smartcampusapi.model.Sensor;
import com.management.smartcampusapi.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList; // Upgraded for Thread Safety

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    public Response getAllReadings() {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            return buildError(Response.Status.NOT_FOUND, "Sensor '" + sensorId + "' does not exist.");
        }

        // Safe fallback using thread-safe list
        List<SensorReading> readings = DataStore.sensorReadings.getOrDefault(sensorId, new CopyOnWriteArrayList<>());
        return Response.ok(readings).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            return buildError(Response.Status.NOT_FOUND, "Sensor '" + sensorId + "' does not exist.");
        }

        // Part 5 - State Constraint: sensor must be ACTIVE to accept readings
        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        if (reading == null) {
            return buildError(Response.Status.BAD_REQUEST, "Reading body is required.");
        }

        // Auto-generate ID and timestamp if not provided
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Save the reading safely (preventing ConcurrentModificationException)
        DataStore.sensorReadings
                .computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>())
                .add(reading);

        // Side effect: update the parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Reading recorded successfully.");
        response.put("reading", reading);
        response.put("updatedSensorValue", sensor.getCurrentValue());

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    // GET /api/v1/sensors/{sensorId}/readings/{readingId}
    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            return buildError(Response.Status.NOT_FOUND, "Sensor '" + sensorId + "' does not exist.");
        }

        List<SensorReading> readings = DataStore.sensorReadings.getOrDefault(sensorId, new CopyOnWriteArrayList<>());

        // Stream simplification: Finds the specific reading in a single, clean line
        SensorReading found = readings.stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst()
                .orElse(null);

        if (found == null) {
            return buildError(Response.Status.NOT_FOUND, "Reading '" + readingId + "' not found for sensor '" + sensorId + "'.");
        }

        return Response.ok(found).build();
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