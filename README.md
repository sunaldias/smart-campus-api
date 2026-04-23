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