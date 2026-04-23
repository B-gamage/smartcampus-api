## Smart Campus Sensor & Room Management API

The Smart Campus API manages campus Rooms and their deployed IoT Sensors. It provides a versioned REST interface at `/api/v1` with full CRUD operations, sub-resource nesting for historical sensor readings, and a robust error-handling layer.

| Resource | Base Path |
|---|---|
| Discovery | `GET /api/v1` |
| Rooms | `/api/v1/rooms` |
| Sensors | `/api/v1/sensors` |
| Readings | `/api/v1/sensors/{id}/readings` |

## Technology Stack
- Java 11
- JAX-RS 2.1 via Jersey 2.41
- Apache Tomcat 9
- Jackson for JSON
- Maven build system
- In-memory storage using ConcurrentHashMap

 ### How to Build & Run

### Prerequisites
- Java 11+
- Maven 3.6+
- Apache Tomcat 9

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/B-gamage/smartcampus-api.git
cd smartcampus-api

# 2. Build the WAR file
mvn clean package

# 3. Copy WAR to Tomcat
cp target/smartcampus-api-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/

# 4. Start Tomcat
$TOMCAT_HOME/bin/startup.sh
Server starts at http://localhost:8080/smartcampus-api/api/v1

Sample curl Commands

1. Discovery endpoint

curl http://localhost:8080/smartcampus-api/api/v1
2. List all rooms

curl http://localhost:8080/smartcampus-api/api/v1/rooms
3. Create a new room

curl -X POST http://localhost:8080/smartcampus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CAFE-01","name":"Campus Cafe","capacity":80}'
4. Create a sensor

curl -X POST http://localhost:8080/smartcampus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-099","type":"Temperature","status":"ACTIVE","currentValue":20.0,"roomId":"LIB-301"}'
5. Post a reading

curl -X POST http://localhost:8080/smartcampus-api/api/v1/sensors/TEMP-099/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}'
6. Get reading history

curl http://localhost:8080/smartcampus-api/api/v1/sensors/TEMP-099/readings
7. Filter sensors by type

curl "http://localhost:8080/smartcampus-api/api/v1/sensors?type=Temperature"
8. Delete room with sensors (expect 409)

curl -X DELETE http://localhost:8080/smartcampus-api/api/v1/rooms/LIB-301
9. Reading on MAINTENANCE sensor (expect 403)

curl -X POST http://localhost:8080/smartcampus-api/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":5.0}'
10. Sensor with fake roomId (expect 422)

curl -X POST http://localhost:8080/smartcampus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-999","type":"CO2","roomId":"GHOST-999"}'
Report: Answers to Coursework Questions

Part 1.1 – JAX-RS Resource Lifecycle

By default, JAX-RS creates a new instance of every resource class for each incoming HTTP request (per-request scope). This is intentional — it avoids shared mutable state on the resource object itself, making each request independent and thread-safe at the instance level. However, this creates a challenge for in-memory data storage. If rooms and sensors were stored as instance fields on the resource class, every request would start with empty data. To solve this, all shared state is held in the DataStore singleton. The DataStore uses ConcurrentHashMap instead of HashMap to handle concurrent read/write operations safely — for example, when two requests simultaneously try to register a sensor, ConcurrentHashMap's atomic operations prevent a race condition or data corruption.

Part 1.2 – HATEOAS

HATEOAS (Hypermedia as the Engine of Application State) is considered the highest maturity level (Level 3) in Richardson's REST Maturity Model. Rather than forcing client developers to consult external static documentation, a HATEOAS-compliant API embeds navigational links directly inside each response. A client can start at the discovery endpoint and follow links to rooms, sensors, and readings without needing any prior knowledge of the URL structure. This benefits client developers significantly: the API becomes self-describing, clients are decoupled from hard-coded URLs so the server can change paths without breaking clients, and the cognitive overhead of learning the API is reduced.

Part 2.1 – Full Objects vs IDs in List Responses

Returning full room objects in a list response means the client gets all the data it needs in a single network round-trip, reducing latency for rendering a dashboard. However, for large datasets with many fields, this increases payload size and bandwidth consumption. Returning only IDs minimises the list payload but forces the client to issue a separate GET request for every room it needs details on — this is the N+1 request problem and can severely impact performance with hundreds of rooms. A pragmatic middle ground is to return a lightweight summary in the list and reserve the full object for individual GET fetches.

Part 2.2 – Idempotency of DELETE

Yes, DELETE is idempotent per the HTTP specification. Idempotency means that sending the same request multiple times produces the same server state as sending it once. In this implementation: the first DELETE of a room removes it and returns 204 No Content. The second DELETE finds no room and returns 404 Not Found. The server state after both calls is identical — the room does not exist. The response code differs but the state effect is the same, which is the correct interpretation of idempotency. No data is corrupted or duplicated by repeated calls, making the operation safe to retry in unreliable networks.

Part 3.1 – @Consumes(APPLICATION_JSON) and Media Type Mismatch

When @Consumes(MediaType.APPLICATION_JSON) is declared on a method, JAX-RS checks the Content-Type header of every incoming request against this annotation before the method is invoked. If a client sends Content-Type: text/plain or Content-Type: application/xml, the runtime determines that no resource method matches the incoming media type and automatically returns HTTP 415 Unsupported Media Type without ever entering the method body. This protects the API from malformed or unexpected payloads and means the developer does not need to manually validate the content type inside every method.

Part 3.2 – @QueryParam vs Path Segment for Filtering

Using @QueryParam for filtering (GET /sensors?type=CO2) is the correct RESTful design because query parameters are semantically intended for optional, non-identifying operations like filtering, sorting, and pagination. The base resource (/sensors) still has a clear identity representing the full collection. Using a path segment (/sensors/type/CO2) implies that type/CO2 is a sub-resource — a distinct entity with its own identity — which is architecturally misleading. It also makes combining multiple filters harder. Query parameters are optional by nature, so a client omitting the parameter gracefully retrieves all sensors, whereas a path-based approach requires a completely separate route for the unfiltered case.

Part 4.1 – Sub-Resource Locator Pattern

The Sub-Resource Locator pattern delegates further request handling to a dedicated class at runtime rather than defining all nested paths in one monolithic resource class. The benefits are: (1) Separation of concerns — SensorResource manages sensor CRUD while SensorReadingResource manages reading logic, each with a single responsibility. (2) Reusability — SensorReadingResource could be reused in other contexts. (3) Testability — each class can be unit-tested in isolation. (4) Maintainability — adding new reading operations only requires changes to SensorReadingResource. Without this pattern, a single class handling all nested paths would quickly become unmanageable as the API grows.

Part 5.2 – HTTP 422 vs 404

HTTP 404 Not Found is semantically correct when the requested URL itself does not correspond to any resource. HTTP 422 Unprocessable Entity is more accurate when the request URL is valid and parseable but the payload contains a logical error — in this case a roomId field referencing a non-existent room. The request was well-formed JSON sent to a valid endpoint (POST /api/v1/sensors), so 404 would be misleading. The sensors endpoint does exist. The problem is inside the payload: a foreign-key reference that cannot be resolved. 422 communicates precisely that the server understood the request structure but could not process it due to a semantic dependency violation.

Part 5.4 – Security Risks of Exposing Stack Traces

Exposing raw Java stack traces creates several serious security risks: (1) Technology fingerprinting — stack traces reveal the server's technology stack including Java version and framework versions, allowing attackers to look up known CVEs. (2) Internal path disclosure — file paths, package names, and class names reveal internal architecture aiding targeted attacks. (3) Logic inference — exception messages and line numbers let attackers understand how the code branches, making it easier to craft inputs triggering specific error paths. (4) The Global Exception Mapper ensures all unexpected errors return a generic 500 message while the full trace is logged server-side only where authorised engineers can access it.

Part 5.5 – Filters vs Manual Logging

Using JAX-RS filters for cross-cutting concerns like logging is superior to inserting Logger.info() calls inside every resource method because: (1) DRY principle — the logging format is defined once, changing it requires editing one class not dozens. (2) Guaranteed execution — a filter runs for every matched request automatically, a developer cannot forget to add logging to a new method. (3) Separation of concerns — resource methods remain focused entirely on business logic improving readability and testability. (4) Composability — multiple filters can be chained and ordered without coupling them to business code. (5) Consistency — all requests are logged with a uniform structure making log parsing and monitoring more effective.

