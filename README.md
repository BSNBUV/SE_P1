# Astra Command

Astra Command is an adapter-based, simulation-only ground-control lab prototype for the workflow:

`PLAN -> VALIDATE -> EXECUTE -> MONITOR -> REPLAY`

It uses React, TypeScript, Vite, Leaflet, Java Spring Boot, Spring Data JPA, PostgreSQL, Redis, and Docker Compose. The prototype does not control a physical drone and does not claim any AI-based flight capability. Feasibility checks are deterministic, configurable rules.

## Demo Credentials

- Admin: `admin` / `AstraAdmin!2026`
- Operator: `operator` / `AstraOperator!2026`

## Architecture

- React dashboard calls the Spring Boot REST API.
- Spring Boot owns authentication, authorization, validation, simulator commands, telemetry parsing, persistence, reports, and logs.
- PostgreSQL stores users, missions, waypoints, telemetry, alerts, and mission logs.
- Redis records asynchronous validation job submissions. The worker runs in Spring Boot to resolve the project document's "Spring Boot + Bull/Redis" wording without adding a Node backend solely for Bull.
- `MissionAdapter` and `TelemetryParser` isolate simulator details from core mission logic.

## Run With Docker

```bash
docker compose up --build
```

Open `http://localhost:5173`.

## Local Development

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

If PostgreSQL/Redis are not running locally, use the in-memory demo mode:

```bash
cd backend
SPRING_DATASOURCE_URL='jdbc:h2:mem:astra-local;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1' \
SPRING_DATASOURCE_USERNAME=sa \
SPRING_DATASOURCE_PASSWORD='' \
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver \
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.H2Dialect \
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Open the frontend at `http://localhost:5173`. `http://localhost:8080/api/health` is only the backend health JSON.

## Five-Minute Demo

1. Login as `operator`.
2. Open Mission Planner.
3. Generate Demo Route or click the map to add waypoints.
4. Save the mission.
5. Open Validation and click Validate Mission.
6. Review per-rule PASS/WARNING/REJECT, distance, battery calculation, and measured MVL.
7. Open Live Mission and start execution; the launch sequence shows the adapter/SITL handoff.
8. Watch persisted backend telemetry move the SITL marker.
9. Trigger Simulate Low Battery and verify alert/log creation.
10. Trigger Emergency Stop or let the mission complete.
11. Open Replay and play stored telemetry.
12. Open Reports and download the mission PDF.

## Validation Logic

The validation pipeline is:

`Mission Input -> Waypoint Check -> Route Check -> Battery/Feasibility Check -> PASS/WARNING/REJECT`

Battery is estimated as:

`battery_used = base_cost + distance_km * cost_per_km + altitude_factor`

Thresholds live in Spring configuration and can be changed with environment variables. The 15% low-battery alert threshold is used only for simulator safety alerting.

## Mission Lifecycle

`CREATED -> VALIDATING -> VALIDATED -> EXECUTING -> COMPLETED`

Failure/stop states:

`EXECUTING -> FAILURE / STOPPED`

## API

See [docs/api.md](docs/api.md).

## Testing

See [docs/testing.md](docs/testing.md).

## Known Limitations

- This is a deterministic SITL-style prototype, not an aircraft controller.
- Redis is used to back the validation job record; the worker itself is implemented in Spring Boot.
- Metrics are actual current-run measurements. The UI does not claim target achievement unless data exists.
- Multi-vehicle support is represented by an extensible vehicle/session identifier and adapter boundary, not full multi-aircraft orchestration.
