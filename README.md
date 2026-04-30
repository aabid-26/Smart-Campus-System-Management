# Smart Campus System: Sensor & Room Management API

## Project Overview
This project serves as the robust backend infrastructure for a university's "Smart Campus" initiative. Designed to scale from a small pilot project to a comprehensive campus-wide system, this RESTful API manages thousands of physical rooms and the diverse array of IoT hardware within them, including CO2 monitors, occupancy trackers, and smart lighting controllers. 

Built strictly using **Java and JAX-RS (Jakarta RESTful Web Services)**, the API acts as a seamless, high-performance interface allowing campus facilities managers and automated building systems to interact with live campus data. To ensure thread safety in a multi-threaded web server environment without relying on external databases, the system utilizes `ConcurrentHashMap` structures for reliable, in-memory data storage.


The API follows core RESTful principles including:
- Resource-based URIs
- Standard HTTP methods (GET, POST, DELETE)
- JSON request and response bodies
- Appropriate HTTP status codes
- Structured error handling with no stack trace exposure
- Sub-resource nesting for sensor readings
- HATEOAS-style discovery endpoint

All data is stored in-memory using ConcurrentHashMap data structures. 
No database is required.


## Technology Stack

|      Technology      |                Purpose                 |
|----------------------|----------------------------------------|
| Java 8               | Core programming language              |
| JAX-RS (Jersey 2.41) | RESTful API framework                  |
| Apache Tomcat 9      | Servlet container and web server       |
| Maven                | Build and dependency management        |
| Jackson              | JSON serialization and deserialization |
| ConcurrentHashMap    | Thread-safe in-memory data storage     |

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


## HTTP Status Codes

| Code |        Meaning        |                     When It Occurs                    |
|------|-----------------------|-------------------------------------------------------|
| 200 | OK                     | Successful GET or DELETE                              |
| 201 | Created                | Successful POST                                       |
| 400 | Bad Request            | Missing required fields                               |
| 403 | Forbidden              | Posting a reading to a MAINTENANCE or OFFLINE sensor  |
| 404 | Not Found              | Resource does not exist                               |
| 409 | Conflict               | Deleting a room that still has sensors                |
| 415 | Unsupported Media Type | Sending non-JSON content                              |
| 422 | Unprocessable Entity   | Creating a sensor with a non-existent roomId          |
| 500 | Internal Server Error  | Any unexpected server error                           |

  
## How to Build and Run

### Prerequisites

Before running the project make sure you have the following installed:

- Java 8 or higher
- Apache Maven 3.6 or higher
- Apache Tomcat 9 (optional if using embedded server)
- Git

You can verify your installations by running:

```bash
java -version
mvn -version
```

---

### Step 1 — Clone the Repository

```bash
git clone https://github.com/yourusername/SmartCampusAPI.git
cd SmartCampusAPI
```

---

### Step 2 — Build the Project

Run the following Maven command to compile the project and package it into a WAR file:

```bash
mvn clean install
```

If the build is successful you will see:
BUILD SUCCESS

The WAR file will be generated at:
target/SmartCampusAPI-1.0-SNAPSHOT.war

### Step 3 — Run the Server
#### Running from Apache NetBeans

1. Open the project in Apache NetBeans
2. Right-click the project in the Projects panel
3. Click **Clean and Build**
4. Right-click again and click **Run**
5. The server will start automatically

---

### Step 4 — Verify the Server is Running

Open your browser or Postman and navigate to:
http://localhost:8080/SmartCampusAPI/api/v1

You should receive a JSON response like this:

```json
{
    "api": "Smart Campus Sensor & Room Management API",
    "version": "1.0",
    "adminContact": "admin@smartcampus.ac.uk",
    "resources": {
        "rooms": "/api/v1/rooms",
        "sensors": "/api/v1/sensors"
    }
}
```


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


  ---

## Conceptual Coursework Report

---

### Part 1: Service Architecture & Setup

#### 1.1 Project & Application Configuration

**Question:** In your report, explain the default lifecycle of a JAX-RS Resource class. 
Is a new instance instantiated for every incoming request, or does the runtime treat it 
as a singleton? Elaborate on how this architectural decision impacts the way you manage 
and synchronize your in-memory data structures (maps/lists) to prevent data loss or 
race conditions.

**Answer:**

By default, JAX-RS follows a per-request lifecycle, meaning a brand new instance of 
each resource class is instantiated every time an HTTP request arrives and is destroyed 
once the response is sent. This is the default behaviour unless the class is explicitly 
annotated with @Singleton, which causes the runtime to create only one shared instance 
across all requests.

This architectural decision has a direct impact on how in-memory data must be managed. 
Since each request gets its own resource instance, instance-level fields cannot be used 
to store shared state, as the class instance is destroyed after the request ends. If maps 
and lists are used as instance variables, any data stored within them will be lost the 
moment the request completes. To maintain persistent state across requests, data 
structures must be declared as static fields in a shared class such as DataStore, so 
they persist beyond the lifetime of any single resource instance.

In this implementation, DataStore holds three static ConcurrentHashMap collections for 
rooms, sensors, and readings. Because multiple requests can arrive simultaneously, each 
handled by a different resource instance on a different thread, using a plain HashMap 
risks race conditions where two threads read and write simultaneously, causing corrupted 
or lost data. ConcurrentHashMap is thread-safe by design, allowing concurrent reads and 
writes without explicit synchronisation blocks, preventing data loss and ensuring 
consistency across all in-flight requests.

---

#### 1.2 The Discovery Endpoint

**Question:** Why is the provision of "Hypermedia" (links and navigation within 
responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this 
approach benefit client developers compared to static documentation?

**Answer:**

HATEOAS (Hypermedia As The Engine Of Application State) is considered a hallmark of 
advanced RESTful design because it makes an API self-describing and navigable. Rather 
than requiring a client to have prior knowledge of every available URL, a 
HATEOAS-compliant response embeds links directly in the JSON, telling the client where 
it can go next from its current state. A client does not need to hardcode those paths, 
as they can be read dynamically from the response itself.

This approach benefits client developers in several important ways compared to static 
documentation:

1. **Reduces coupling** — When the URL structure changes server-side, the client 
automatically receives the updated link without needing a documentation update or a 
code change.

2. **Lowers the barrier to entry** — A new developer can start at the root endpoint 
and discover the entire API just by following the links in each response, without 
needing to read any external documentation.

3. **Improves resilience** — When the API evolves, HATEOAS-driven clients adapt 
naturally by following whatever links the server currently provides, whereas clients 
built against static documentation break when the API changes.

---

### Part 2: Room Management

#### 2.1 Room Resource Implementation

**Question:** When returning a list of rooms, what are the implications of returning 
only IDs versus returning the full room objects? Consider network bandwidth and 
client-side processing.

**Answer:**

When designing a GET /rooms endpoint, there is a meaningful trade-off between returning 
only a list of room IDs versus returning the full room objects.

Returning only IDs minimises the response payload size, which reduces network bandwidth 
consumption. However, it forces the client to make a separate GET /rooms/{id} request 
for each ID in order to retrieve any useful information. This is known as the N+1 
request problem — for 100 rooms the client must make 101 total requests, significantly 
increasing latency and placing additional load on the server.

Returning full room objects increases the size of a single response but eliminates the 
need for any follow-up requests. The client receives all the information it needs in one 
round trip, which is far more efficient in practice. It also reduces client-side 
complexity, since the client does not need to implement logic to iterate over IDs and 
fetch each resource individually.

In this implementation, GET /api/v1/rooms returns full room objects. This is the more 
practical and client-friendly approach, particularly for a campus management system 
where a facilities manager would typically want to see room names, capacities, and 
sensor counts at a glance rather than a bare list of IDs.

---

#### 2.2 Room Deletion & Safety Logic

**Question:** Is the DELETE operation idempotent in your implementation? Provide a 
detailed justification by describing what happens if a client mistakenly sends the 
exact same DELETE request for a room multiple times.

**Answer:**

The DELETE operation is partially idempotent in this implementation. Idempotency means 
that making the same request multiple times produces the same server state as making 
it once.

For example, if a client sends DELETE /api/v1/rooms/T001 and the room exists with no 
sensors assigned, it is successfully deleted and a 200 OK is returned. If the same 
request is sent a second time, the room no longer exists and the server returns a 
404 Not Found. The key distinction is that idempotency refers to the server state, 
not the HTTP response code. The desired outcome — the room no longer existing — is 
achieved regardless of how many times the request is sent.

This is a widely accepted and common pattern in REST API design. No duplicate deletions 
occur, no data is corrupted, and no orphaned sensors are created by sending the same 
DELETE request multiple times. The implementation is therefore considered idempotent 
in the RESTful sense.

---

### Part 3: Sensor Operations & Linking

#### 3.1 Sensor Resource & Integrity

**Question:** We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation 
on the POST method. Explain the technical consequences if a client attempts to send 
data in a different format, such as text/plain or application/xml. How does JAX-RS 
handle this mismatch?

**Answer:**

The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS that the POST 
endpoint only accepts request bodies with a Content-Type of application/json. If a 
client sends a request with a different content type, such as text/plain or 
application/xml, JAX-RS handles the mismatch automatically before the method is 
even invoked.

Specifically, the JAX-RS runtime inspects the Content-Type header of the incoming 
request and attempts to find a registered resource method whose @Consumes declaration 
matches it. If no match is found, the runtime immediately rejects the request at the 
framework level and returns an HTTP 415 Unsupported Media Type response without 
executing any application code whatsoever.

This is an important safety mechanism because it prevents malformed or unexpected data 
formats from reaching the business logic layer. Without @Consumes, a client sending 
XML or plain text could cause deserialization failures or NullPointerExceptions deep 
inside the method, which are significantly harder to debug and less informative to the 
client. By declaring @Consumes explicitly, the API communicates its contract clearly 
and enforces it at the framework level, providing a clean and standardised error 
response for any client that violates it.

---

#### 3.2 Filtered Retrieval & Search

**Question:** You implemented this filtering using @QueryParam. Contrast this with an 
alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). 
Why is the query parameter approach generally considered superior for filtering and 
searching collections?

**Answer:**

Using @QueryParam for filtering, as in GET /api/v1/sensors?type=CO2, is generally 
considered superior to embedding the filter value in the URL path, such as 
GET /api/v1/sensors/type/CO2, for several reasons.

**Semantic correctness** is the primary reason. A path segment identifies a specific 
resource, while a query parameter modifies or refines a request for a collection. 
Filtering is inherently a refinement operation — the client is asking for a subset of 
the /sensors collection, not navigating to a distinct resource called type/CO2. Using 
a path for this purpose violates REST's resource-naming conventions.

**Optionality** is another key advantage. With @QueryParam, the parameter is entirely 
optional — GET /api/v1/sensors returns all sensors, and GET /api/v1/sensors?type=CO2 
returns a filtered subset. This is handled cleanly in JAX-RS since a missing query 
parameter simply resolves to null. With a path-based approach, the filter becomes 
mandatory and part of the URL structure, making it impossible to retrieve an unfiltered 
list from the same endpoint without adding a separate route.

**Composability** is also stronger with query parameters. Multiple filters can be 
combined naturally, for example ?type=CO2&status=ACTIVE, without changing the URL 
structure. Achieving the same with path parameters would require a complex and rigid 
URL design that becomes increasingly difficult to maintain and extend.

Finally, path-based filters can cause **routing conflicts** in JAX-RS, where 
/{sensorId} and /type/{value} are ambiguous to the framework, potentially causing 
the wrong method to be invoked entirely.

---

### Part 4: Deep Nesting with Sub-Resources

#### 4.1 The Sub-Resource Locator Pattern

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. 
How does delegating logic to separate classes help manage complexity in large APIs 
compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one 
massive controller class?

**Answer:**

The Sub-Resource Locator pattern is an architectural approach in JAX-RS where a 
resource method does not handle an HTTP request directly. Instead it returns an 
object — another resource class — that JAX-RS then uses to continue resolving the 
remaining path. In this implementation, SensorResource contains a locator method 
annotated with @Path("/{sensorId}/readings") that returns a new instance of 
SensorReadingResource, which then handles the actual GET and POST operations on 
readings.

The primary architectural benefit of this pattern is separation of concerns. Each 
resource class has a single, well-defined responsibility. SensorResource manages the 
sensor collection, and SensorReadingResource exclusively manages reading history for 
a given sensor. Neither class needs to know about the internal details of the other 
beyond the handoff point.

In a large API, the alternative — defining every nested path inside one monolithic 
controller — becomes extremely difficult to manage. A single class handling /sensors, 
/sensors/{id}, /sensors/{id}/readings, and /sensors/{id}/readings/{rid} would grow 
rapidly in size, making it harder to read, test, and maintain. Any change to reading 
logic would require modifying the same file as sensor logic, increasing the risk of 
introducing bugs in unrelated functionality.

The Sub-Resource Locator pattern resolves this by enabling modular composition. Each 
class can be developed, tested, and reasoned about independently. It also improves the 
scalability of the codebase — adding new sub-resources such as /sensors/{id}/alerts 
in future only requires creating a new class and adding one locator method, without 
modifying any existing code. This aligns directly with the Single Responsibility 
Principle and makes the overall API architecture far more maintainable as complexity 
grows.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### 5.1 Dependency Validation (422 Unprocessable Entity)

**Question:** Why is HTTP 422 often considered more semantically accurate than a 
standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:**

HTTP 404 Not Found conventionally means that the resource identified by the request 
URL does not exist. For example, making a request to GET /api/v1/rooms/UNKNOWN-ID 
correctly returns a 404 because the URL itself points to something that cannot be 
found on the server.

However, when a client sends a POST /api/v1/sensors request with a valid URL and a 
well-formed JSON body, but includes a roomId that does not exist, the situation is 
fundamentally different. The endpoint /api/v1/sensors exists and is reachable. The 
JSON body is syntactically valid and correctly formatted. The problem is not that 
anything is missing from the server's URL space — the problem is that the content of 
the payload references an entity that does not exist.

This is precisely the scenario HTTP 422 Unprocessable Entity was designed for. It 
signals that the server understood the request, successfully parsed the body, but was 
unable to process it due to a semantic validation failure. The distinction is 
meaningful — 404 describes a problem with the URL, while 422 describes a problem with 
the data inside a valid request.

From a client developer's perspective, 422 is far more informative. Receiving a 404 
in response to a POST request would be misleading — the developer might incorrectly 
assume the endpoint itself does not exist, rather than understanding that a referenced 
value inside their payload is invalid. A 422 response, especially when paired with a 
descriptive JSON error body identifying the problematic field and value, allows the 
developer to immediately identify and correct the issue and resubmit the request 
successfully.

---

#### 5.2 The Global Safety Net (500)

**Question:** From a cybersecurity standpoint, explain the risks associated with 
exposing internal Java stack traces to external API consumers. What specific 
information could an attacker gather from such a trace?

**Answer:**

Exposing raw Java stack traces to external API consumers represents a significant 
cybersecurity risk because stack traces contain detailed internal information about 
the application that was never intended to be public.

**Internal path disclosure** is one of the most immediate risks. A stack trace 
typically reveals the full directory structure of the server, including file paths 
such as /home/ubuntu/SmartCampusAPI/src/main/java/com/management. This tells an 
attacker the operating system, the deployment directory, and the package structure 
of the application, all of which contribute to building a more accurate picture of 
the target environment.

**Technology and version fingerprinting** is another serious concern. Stack traces 
expose the exact names and versions of frameworks and libraries in use, such as 
jersey-server-2.41 or jackson-databind-2.13. An attacker can cross-reference these 
versions against public vulnerability databases such as the CVE registry to identify 
known exploits that apply specifically to those versions, effectively turning an 
information leak into a direct attack vector.

**Application logic disclosure** is perhaps the most dangerous risk. A stack trace 
reveals the exact sequence of method calls that led to the error, including class 
names, method names, and line numbers. This exposes the internal business logic and 
control flow of the application, allowing an attacker to identify weak points, 
understand how data is processed, and craft targeted inputs designed to trigger 
specific failure modes such as injection attacks or authentication bypasses.

For these reasons, the GlobalExceptionMapper in this implementation intercepts all 
unhandled exceptions, logs the full stack trace server-side only for legitimate 
debugging purposes, and returns only a generic, safe message to the client with no 
internal details whatsoever.

---

#### 5.3 API Request & Response Logging Filters

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns 
like logging, rather than manually inserting Logger.info() statements inside every 
single resource method?

**Answer:**

Using JAX-RS filters for cross-cutting concerns like logging is significantly more 
advantageous than manually inserting Logger.info() statements inside individual 
resource methods, for several important reasons.

**Avoiding code duplication** is the most immediate benefit. In an API with numerous 
endpoints, adding logging to every method individually means writing and maintaining 
the same boilerplate in many different places. If the log format ever needs to change, 
every single method would need to be updated individually, which is error-prone and 
time-consuming. A filter centralises this logic in one class, meaning any change is 
made once and applies everywhere automatically.

**Separation of concerns** is a core software engineering principle that filters 
directly support. Resource methods should focus exclusively on their business logic — 
creating rooms, registering sensors, recording readings. Mixing logging statements 
into business logic makes the code harder to read and violates the principle that 
each component should have one clear responsibility. Filters handle observability as 
a completely separate concern that sits outside the resource layer entirely.

**Guaranteed execution** is another critical advantage. A manually placed 
Logger.info() statement is susceptible to accidental deletion, omission in a new 
method, or being bypassed by an early return statement. A registered JAX-RS filter 
is invoked by the framework for every single request and response without exception, 
ensuring complete and consistent coverage across the entire API regardless of how 
many endpoints exist or are added in future.

**Consistency** is the final benefit. Because the filter captures the HTTP method, 
URI, and status code directly from the request and response contexts, the log format 
is uniform across all endpoints. Manual logging tends to produce inconsistent messages 
as different developers write log statements in different styles, making logs 
significantly harder to parse and analyse over time.
