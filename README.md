# Smart Campus System: Sensor & Room Management API

## 📖 Project Overview
[cite_start]This project serves as the robust backend infrastructure for a university's "Smart Campus" initiative[cite: 24]. [cite_start]Designed to scale from a small pilot project to a comprehensive campus-wide system, this RESTful API manages thousands of physical rooms and the diverse array of IoT hardware within them, including CO2 monitors, occupancy trackers, and smart lighting controllers[cite: 34]. 

[cite_start]Built strictly using **Java and JAX-RS (Jakarta RESTful Web Services)**[cite: 35], the API acts as a seamless, high-performance interface allowing campus facilities managers and automated building systems to interact with live campus data. [cite_start]To ensure thread safety in a multi-threaded web server environment without relying on external databases, the system utilizes `ConcurrentHashMap` structures for reliable, in-memory data storage[cite: 207].

---

## ⚙️ Core Architecture & Features

### 1. Versioned API & Discovery (HATEOAS)
* [cite_start]**Entry Point:** The application is strictly versioned under the `/api/v1` path to allow for future backward-compatible updates.
* [cite_start]**Discovery Endpoint:** Features a root discovery endpoint (`GET /api/v1`) that provides API metadata, administrative contact details, and a map of primary resource collections, adhering to advanced RESTful design principles.

### 2. Room Management & Safety Logic
**Resource CRUD:** Full capabilities to retrieve, create, and fetch metadata for university rooms.
**Data Orphan Prevention:** Implements strict business logic to prevent the deletion of any room that still contains active sensors. Attempting to do so triggers a controlled HTTP 409 Conflict response.

### 3. Sensor Linking & Filtered Retrieval
* [cite_start]**Relational Integrity:** When registering a new sensor, the API validates the dependency to ensure the assigned `roomId` actually exists in the system[cite: 129]. [cite_start]Failing this check returns a semantic HTTP 422 Unprocessable Entity[cite: 156].
* [cite_start]**Search & Filtering:** The sensor collection endpoint supports optional query parameters, allowing clients to filter hardware by specific criteria (e.g., `?type=CO2`)[cite: 134].

### 4. Deep Nesting via Sub-Resources
* [cite_start]**Historical Data Tracking:** Utilizes the JAX-RS **Sub-Resource Locator pattern** to cleanly manage historical readings for individual sensors (`/sensors/{id}/readings`)[cite: 141]. This delegates logic to dedicated controllers rather than bloating a single massive file.
* [cite_start]**Side-Effect Synchronization:** Appending a new historical reading automatically triggers an update to the `currentValue` on the parent Sensor object, ensuring system-wide data consistency[cite: 146].

### 5. Advanced Error Handling & Observability
* [cite_start]**Leak-Proof Exception Mapping:** The API never exposes raw Java stack traces (which poses a security risk)[cite: 148, 163]. [cite_start]Custom JAX-RS `ExceptionMappers` intercept runtime exceptions and translate them into professional, semantic JSON responses with appropriate HTTP status codes (e.g., 403 Forbidden for sensors in maintenance [cite: 160][cite_start], 500 Internal Server Error for unhandled faults [cite: 162]).
* [cite_start]**Cross-Cutting Logging:** Implements custom JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter` classes to log the HTTP method, URI, and final status code of every interaction, providing complete API observability without cluttering business logic[cite: 166, 167].

---

## 🛠️ Technology Stack
* **Language:** Java
* **Framework:** JAX-RS (Jakarta RESTful Web Services) 
* **Build Tool:** Maven 
* **Data Storage:** Thread-Safe In-Memory Data Structures (`ConcurrentHashMap`, `CopyOnWriteArrayList`)

## Project Structure
src/main/java/com/management/smartcampusapi/
├── SmartCampusApplication.java       - JAX-RS entry point (@ApplicationPath)
├── data/
│   └── DataStore.java                - Shared in-memory data store
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── resources/
│   ├── DiscoveryResource.java        - GET /api/v1
│   ├── RoomResource.java             - Room CRUD operations
│   ├── SensorResource.java           - Sensor operations + sub-resource locator
│   └── SensorReadingResource.java    - Sensor reading history
├── exceptions/
│   ├── RoomNotEmptyException.java
│   ├── RoomNotEmptyExceptionMapper.java
│   ├── LinkedResourceNotFoundException.java
│   ├── LinkedResourceNotFoundExceptionMapper.java
│   ├── SensorUnavailableException.java
│   ├── SensorUnavailableExceptionMapper.java
│   └── GlobalExceptionMapper.java
└── filter/
    └── LoggingFilter.java

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
  -d '{"id":"ENG-101","name":"Engineering Lab","capacity":40}'

### 4. Get a specific room
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/ENG-101

### 5. Try deleting a room that has sensors (expect 409)
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301

### 6. Create a sensor with a valid roomId
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-003","type":"Temperature","status":"ACTIVE","currentValue":20.0,"roomId":"ENG-101"}'

### 7. Get sensors filtered by type
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature"

### 8. Post a reading for a sensor
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.5}'

### 9. Get all readings for a sensor
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings

### 10. Try posting a reading to a MAINTENANCE sensor (expect 403)
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-002/readings \
  -H "Content-Type: application/json" \
  -d '{"value":19.0}'
