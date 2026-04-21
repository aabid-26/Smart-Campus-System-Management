package com.management.smartcampusapi;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class SmartCampusAPI extends Application {
    // Modern JAX-RS(jersey) uses a feature called Auto-Discovery
    // Declared a class with @ApplicationPath, where the jersey framework will automatically scan the project folder when the server starts
    // Looks for any class that has a @Path annotation .
}
