package com.management.smartcampusapi.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover(@Context UriInfo uriInfo) {
        
        // Dynamically grabs exactly where the server is running (e.g., http://localhost:8080/api/v1/)
        String baseUri = uriInfo.getBaseUri().toString();

        // Using LinkedHashMap to keep the JSON strictly ordered when it prints
        Map<String, Object> response = new LinkedHashMap<>();
        
        // 1. Core API Information
        response.put("api", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0");
        response.put("description", "RESTful API to manage campus rooms and IoT sensors.");
        response.put("developer", "Aabid Zimal"); 
        response.put("contact", "aabid.20240402@iit.ac.lk"); 

        // 3. Dynamic HATEOAS Links (Clean, professional, and not over-cluttered)
        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms", baseUri + "rooms");     
        links.put("sensors", baseUri + "sensors");
        
        response.put("resources", links);

        return Response.ok(response).build();
    }
}