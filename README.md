# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W Client-Server Architectures  
**Student:** w2151897@westminster.ac.uk  
**Technology Stack:** Java, JAX-RS (Jersey), Maven  

---

## Overview

The University of Westminster Smart Campus project uses a RESTful API which this project uses as its foundation. The system provides complete control over campus Rooms and their installed IoT Sensors through its backend system which contains a full historical database of all sensor measurement data. The API uses JAX-RS (Jersey) as its framework which operates on in-memory data storage through ConcurrentHashMap and synchronized ArrayList. The system works without database connections and it does not implement Spring Boot functionality.

**Base URL:** `http://localhost:8080/api/v1`

---

## Project Structure

```text
smart-campus-api/
├── src/main/java/com/smartcampus/
│   ├── config/             # ApplicationConfig - @ApplicationPath setup
│   ├── exception/          # Custom exception classes
│   ├── filter/             # ApiLoggingFilter (request & response)
│   ├── mapper/             # ExceptionMappers (409, 422, 403, 500)
│   ├── model/              # Room, Sensor, SensorReading POJOs
│   ├── resource/           # RoomResource, SensorResource, SensorReadingResource, DiscoveryResource, DebugResource
│   ├── store/              # DataStore - shared in-memory collections
│   └── util/               # ErrorResponse helper
└── pom.xml
```

---

## How to Build and Run

### Prerequisites

* Java 17 or higher
* Maven 3.8 or higher
* Apache Tomcat 9 (or any servlet container)

### Steps

**1. Clone the repository**

```bash
git clone <your-repo-url>
cd smart-campus-api
```

**2. Build the project**

```bash
mvn clean package
```

This produces `target/smart-campus-api.war`.

**3. Deploy to Tomcat**

Copy the WAR file to your Tomcat `webapps` directory:

```bash
cp target/smart-campus-api.war /path/to/tomcat/webapps/
```

**4. Start Tomcat**

```bash
/path/to/tomcat/bin/startup.sh
```

**5. Verify the server is running**

```bash
curl http://localhost:8080/api/v1
```

You should receive a JSON response with API metadata and HATEOAS links.

---

## Sample curl Commands

**1. Discovery endpoint — GET /api/v1**

```bash
curl -X GET http://localhost:8080/api/v1 \
  -H "Accept: application/json"
```

Expected: `200 OK` with API metadata and navigational links.

**2. Create a new room — POST /api/v1/rooms**

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
  "id": "ENG-101",
  "name": "Engineering Lab",
  "capacity": 60
}'
```

Expected: `201 Created` with a `Location` header pointing to the new resource.

**3. Register a new sensor — POST /api/v1/sensors**

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
  "id": "CO2-001",
  "type": "CO2",
  "status": "ACTIVE",
  "currentValue": 450.0,
  "roomId": "LIB-301"
}'
```

Expected: `201 Created`. If `roomId` does not exist, returns `422 Unprocessable Entity`.

**4. Get filtered sensor list — GET /api/v1/sensors?type=Temperature**

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature" \
  -H "Accept: application/json"
```

Expected: `200 OK` with only Temperature sensors in the list.

**5. Post a new sensor reading — POST /api/v1/sensors/TEMP-001/readings**

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
  "id": "R2",
  "timestamp": 1714000000000,
  "value": 23.7
}'
```

Expected: `201 Created`. The parent sensor's `currentValue` is updated to `23.7`.

**6. Delete a room with no sensors — DELETE /api/v1/rooms/ENG-101**

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/ENG-101
```

Expected: `200 OK`. If the room has sensors assigned, returns `409 Conflict`.

**7. Attempt reading on a MAINTENANCE sensor — POST /api/v1/sensors/TEMP-999/readings**

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-999/readings \
  -H "Content-Type: application/json" \
  -d '{
  "id": "R100",
  "timestamp": 1714000000000,
  "value": 19.5
}'
```

Expected: `403 Forbidden` with a structured JSON error body.

**8. Trigger the global 500 handler — GET /api/v1/debug/crash**

```bash
curl -X GET http://localhost:8080/api/v1/debug/crash
```

Expected: `500 Internal Server Error` with a clean JSON body and no Java stack trace.

---

## API Endpoints Reference

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/v1` | Discovery — API metadata and HATEOAS links |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors assigned) |
| GET | `/api/v1/sensors` | List all sensors (optional `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading for a sensor |
| GET | `/api/v1/debug/crash` | Trigger 500 global error handler (demo only) |

---

## HTTP Status Codes Used

| Code | Meaning | When Used |
| --- | --- | --- |
| 200 | OK | Successful GET or DELETE |
| 201 | Created | Successful POST (includes Location header) |
| 400 | Bad Request | Missing or invalid fields in request body |
| 403 | Forbidden | POST reading to a MAINTENANCE sensor |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | DELETE room with assigned sensors / duplicate ID |
| 415 | Unsupported Media Type | Wrong Content-Type sent to endpoint |
| 422 | Unprocessable Entity | Valid JSON but referenced roomId does not exist |
| 500 | Internal Server Error | Unhandled runtime exception |

---

## Report

### Part 1 - Service Architecture & Setup

**Question 1.1 - JAX-RS Resource Lifecycle**

The default request scope of JAX-RS resource classes requires active resource instances to handle their incoming requests which results in the creation of new resource instances for every request that they receive. The system operates this way because request-based information stored in a resource object remains available only to the current user session. The resource instance fields need to be avoided as primary storage locations for application data because these fields do not maintain reliable data storage throughout multiple user sessions. The team needs to store persistent shared state storage for rooms and sensors and sensor readings which they achieve through the shared static DataStore class that provides in-memory storage through ConcurrentHashMap and synchronized ArrayList instead of using resource instance fields. The system needs to manage concurrent access because multiple requests currently use the same shared collections. The system experiences race conditions or inconsistent updates when two requests try to modify the same shared data simultaneously. The system implements its entire operations with thread-safe structures which operate through ConcurrentHashMap and Collections.synchronizedList.

**Question 1.2 — HATEOAS and Hypermedia**

The advanced RESTful design depends on hypermedia because it allows servers to use web links from their response body content to guide clients through their APIs. The API system enables clients to access room links and sensor links and nested reading links without requiring them to manually enter every endpoint from the documentation.
The API system provides client developers with this advantage because it simplifies their process of learning the API and using it in the right way. The system decreases its need for unchanging documentation which becomes obsolete with time. The system provides enhanced flexibility because the server can release new navigation links whenever resource paths or structures undergo changes without needing clients to undergo complete rewriting. The system improves three areas which include discoverability usability and adaptability through its hypermedia system.

---

### Part 2 — Room Management

**Question 2.1 — Returning IDs vs Full Room Objects**

The system will return only room IDs because this approach minimizes response size which enables better network bandwidth preservation that becomes essential when multiple rooms exist or clients require only basic room details. The present system structure forces clients to perform additional tasks because they must submit multiple requests to obtain complete room details. The client benefits from receiving complete room objects because all required details come in one single response. The system reduces request requirements while decreasing client effort needed to complete their work. The response size increases through this process which results in higher bandwidth consumption. Lightweight references need ID returns to function properly while clients need complete object delivery to access detailed room data.

---

**Question 2.2 — Idempotency of DELETE**

Yes, the DELETE operation demonstrates idempotent behavior. Idempotency establishes that sending the identical request multiple times will result in the system reaching the same final state which results from sending it one time. The system will delete the room through its first successful DELETE request because the room contains no active sensors. The API shows 404 Not Found because the room has been deleted when the same DELETE request was sent again. The system state has not changed because the response code differs from the first request because the room remains missing. The operation shows idempotent behavior because repeated identical DELETE requests only permit one system change which happens during the first deletion.

---

### Part 3 — Sensor Operations & Linking

**Question 3.1 — @Consumes and Content-Type Mismatch**