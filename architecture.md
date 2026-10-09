# Astra Command Architecture

## High-Level Architecture

```mermaid
flowchart LR
  Operator --> React[React Dashboard]
  React --> API[Spring Boot REST API]
  API --> Adapter[Adapter Layer]
  Adapter --> Simulator[Deterministic SITL Simulator]
  API <--> PostgreSQL[(PostgreSQL)]
  API <--> Redis[(Redis)]
```

## Command Flow

```mermaid
flowchart LR
  MissionInput[Mission Input] --> Validation[Rule Validation]
  Validation --> Common[Common Mission Format]
  Common --> Adapter[MissionAdapter]
  Adapter --> Simulator[SimulatedSITLAdapter]
```

## Telemetry Flow

```mermaid
flowchart LR
  Simulator --> Parser[Adapter Telemetry Parser]
  Parser --> API[Telemetry API]
  API --> PostgreSQL[(PostgreSQL)]
  API --> Dashboard[Dashboard / Replay]
```

## Mission Lifecycle

```mermaid
stateDiagram-v2
  CREATED --> VALIDATING
  VALIDATING --> VALIDATED
  VALIDATED --> EXECUTING
  EXECUTING --> COMPLETED
  EXECUTING --> STOPPED
  EXECUTING --> FAILURE
```

## Failure Flow

```mermaid
flowchart LR
  LowBattery[Low Battery Telemetry] --> Parser[Telemetry Parser]
  Parser --> Rule[Safety Rule]
  Rule --> Alert[Alert]
  Alert --> UI[Dashboard + Mission Log]
```

The frontend never controls the simulator directly and never connects to PostgreSQL. It talks only to the Spring Boot API. The adapter translates the common Astra mission/telemetry model into the deterministic simulator protocol.
