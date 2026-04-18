package com.management.smartcampusapi;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class SmartCampusAPI extends Application {
    // Extending Application and annotating with @ApplicationPath
    // is enough for Jersey to auto-scan and register all @Path classes.
}
