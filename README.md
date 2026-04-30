# Smart Campus System: Sensor & Room Management API

## Project Overview
This project serves as the robust backend infrastructure for a university's "Smart Campus" initiative. Designed to scale from a small pilot project to a comprehensive campus-wide system, this RESTful API manages thousands of physical rooms and the diverse array of IoT hardware within them, including CO2 monitors, occupancy trackers, and smart lighting controllers. 

Built strictly using **Java and JAX-RS (Jakarta RESTful Web Services)**, the API acts as a seamless, high-performance interface allowing campus facilities managers and automated building systems to interact with live campus data. To ensure thread safety in a multi-threaded web server environment without relying on external databases, the system utilizes `ConcurrentHashMap` structures for reliable, in-memory data storage.

---

## Core Architecture & Features

### 1. Versioned API & Discovery (HATEOAS)
**Entry Point:** The application is strictly versioned under the `/api/v1` path to allow for future backward-compatible updates.
**Discovery Endpoint:** Features a root discovery endpoint (`GET /api/v1`) that provides API metadata, administrative contact details, and a map of primary resource collections, adhering to advanced RESTful design principles.

### 2. Room Management & Safety Logic
**Resource CRUD:** Full capabilities to retrieve, create, and fetch metadata for university rooms.
**Data Orphan Prevention:** Implements strict business logic to prevent the deletion of any room that still contains active sensors. Attempting to do so triggers a controlled HTTP 409 Conflict response.

### 3. Sensor Linking & Filtered Retrieval
**Relational Integrity:** When registering a new sensor, the API validates the dependency to ensure the assigned `roomId` actually exists in the system. Failing this check returns a semantic HTTP 422 Unprocessable Entity.
**Search & Filtering:** The sensor collection endpoint supports optional query parameters, allowing clients to filter hardware by specific criteria (e.g., `?type=CO2`).

### 4. Deep Nesting via Sub-Resources
**Historical Data Tracking:** Utilizes the JAX-RS **Sub-Resource Locator pattern** to cleanly manage historical readings for individual sensors (`/sensors/{id}/readings`). This delegates logic to dedicated controllers rather than bloating a single massive file.
**Side-Effect Synchronization:** Appending a new historical reading automatically triggers an update to the `currentValue` on the parent Sensor object, ensuring system-wide data consistency.

### 5. Advanced Error Handling & Observability
**Leak-Proof Exception Mapping:** The API never exposes raw Java stack traces (which poses a security risk). Custom JAX-RS `ExceptionMappers` intercept runtime exceptions and translate them into professional, semantic JSON responses with appropriate HTTP status codes (e.g., 403 Forbidden for sensors in maintenance [cite: 160][cite_start], 500 Internal Server Error for unhandled faults.
**Cross-Cutting Logging:** Implements custom JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter` classes to log the HTTP method, URI, and final status code of every interaction, providing complete API observability without cluttering business logic.


## Available Endpoints
Base URL: http://localhost:8080/campus_api/api/v1

## API Endpoints

| Method |                Endpoint                |                  Description                 |
|--------|----------------------------------------|----------------------------------------------|
| GET    | `/api/v1`                              | API discovery and metadata                   |
| GET    | `/api/v1/rooms`                        | Get all rooms                                |
| POST   | `/api/v1/rooms`                        | Create a new room                            |
| GET    | `/api/v1/rooms/{roomId}`               | Get a specific room by ID                    |
| DELETE | `/api/v1/rooms/{roomId}`               | Delete a room (only if no sensors assigned)  |
| GET    | `/api/v1/sensors`                      | Get all sensors (supports `?type=` filter)   |
| POST   | `/api/v1/sensors`                      | Register a new sensor                        |
| GET    | `/api/v1/sensors/{sensorId}`           | Get a specific sensor by ID                  |
| DELETE | `/api/v1/sensors/{sensorId}`           | Delete a sensor                              |
| GET    | `/api/v1/sensors/{sensorId}/readings`  | Get all readings for a sensor                |
| POST   | `/api/v1/sensors/{sensorId}/readings`  | Add a new reading for a sensor               |


---

## 🛠️ Technology Stack
* **Language:** Java
* **Framework:** JAX-RS (Jakarta RESTful Web Services) 
* **Build Tool:** Maven 
* **Data Storage:** Thread-Safe In-Memory Data Structures (`ConcurrentHashMap`, `CopyOnWriteArrayList`)

## How to Build and Run

### Prerequisites
- Java 8 or higher installed
- Apache Maven installed
- Apache Tomcat 9 installed

### Steps

1. Clone the repository:
   git clone https://github.com/yourusername/SmartCampusAPI.git
   cd SmartCampusAPI

2. Build the project:
   mvn clean install

3. Run using the embedded Tomcat Maven plugin:
   mvn tomcat7:run

4. The API will be available at:
   http://localhost:8080/SmartCampusAPI/api/v1

### Alternatively — Deploy to Tomcat manually:
1. Run: mvn clean package
2. Copy the generated WAR file from /target/SmartCampusAPI-1.0-SNAPSHOT.war
   into your Tomcat /webapps directory
3. Start Tomcat: ./bin/startup.sh (Mac/Linux) or bin\startup.bat (Windows)
4. Access at: http://localhost:8080/SmartCampusAPI/api/v1


## Sample curl Commands

### 1. Discovery Endpoint
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1

### 2. Get all rooms
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms

### 3. Create a new room
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"R001","name":"Library","capacity":200}'

### 4. Get a specific room
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/R001

### 5. Try deleting a room that has sensors (expect 409)
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/R001

### 6. Create a sensor with a valid roomId
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-003","type":"Temperature","status":"ACTIVE","currentValue":20.0,"roomId":"ENG-101"}'

### 7. Get sensors filtered by type
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature"

### 8. Post a reading for a sensor
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEM001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.5}'

### 9. Get all readings for a sensor
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEM001/readings

### 10. Try posting a reading to a MAINTENANCE sensor (expect 403)
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/OCC001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":19.0}'
