# Smart Campus System: Sensor & Room Management API

## 📖 Project Overview
[cite_start]This project serves as the robust backend infrastructure for a university's "Smart Campus" initiative[cite: 24]. [cite_start]Designed to scale from a small pilot project to a comprehensive campus-wide system, this RESTful API manages thousands of physical rooms and the diverse array of IoT hardware within them, including CO2 monitors, occupancy trackers, and smart lighting controllers[cite: 34]. 

[cite_start]Built strictly using **Java and JAX-RS (Jakarta RESTful Web Services)**[cite: 35], the API acts as a seamless, high-performance interface allowing campus facilities managers and automated building systems to interact with live campus data. [cite_start]To ensure thread safety in a multi-threaded web server environment without relying on external databases, the system utilizes `ConcurrentHashMap` structures for reliable, in-memory data storage[cite: 207].

---

## ⚙️ Core Architecture & Features

### 1. Versioned API & Discovery (HATEOAS)
* [cite_start]**Entry Point:** The application is strictly versioned under the `/api/v1` path to allow for future backward-compatible updates[cite: 104].
* [cite_start]**Discovery Endpoint:** Features a root discovery endpoint (`GET /api/v1`) that provides API metadata, administrative contact details, and a map of primary resource collections, adhering to advanced RESTful design principles[cite: 109].

### 2. Room Management & Safety Logic
* [cite_start]**Resource CRUD:** Full capabilities to retrieve, create, and fetch metadata for university rooms[cite: 115, 116, 117].
* [cite_start]**Data Orphan Prevention:** Implements strict business logic to prevent the deletion of any room that still contains active sensors[cite: 122]. [cite_start]Attempting to do so triggers a controlled HTTP 409 Conflict response[cite: 153].

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
