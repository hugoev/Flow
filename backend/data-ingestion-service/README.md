# Data Ingestion Service

A high-performance microservice for ingesting vehicle telemetry data using Spring Boot and Apache Kafka.

## Overview

This service provides RESTful APIs for receiving telemetry data from vehicles and publishing it to Kafka for further processing. It follows clean architecture principles with clear separation of concerns.

## Architecture

The service is structured following clean architecture principles:

- **Presentation Layer**: REST controllers handling HTTP requests
- **Service Layer**: Business logic for data processing and validation
- **Infrastructure Layer**: Kafka configuration and external integrations
- **Domain Layer**: Core business models and entities

## API Endpoints

### POST /api/telemetry/ingest

Ingests a single telemetry data point.

**Request Body:**

```json
{
  "vehicleId": "VH001",
  "timestamp": "2023-12-07T10:30:00Z",
  "latitude": 40.7128,
  "longitude": -74.006,
  "speed": 65.5,
  "fuelLevel": 85.2,
  "engineTemp": 90.5,
  "tirePressure": 32.0
}
```

**Response:** `202 Accepted` with confirmation message

### POST /api/telemetry/ingest/batch

Ingests multiple telemetry data points in a single request.

**Request Body:** Array of telemetry data objects

**Response:** `202 Accepted` with processing summary

### GET /api/telemetry/health

Health check endpoint.

**Response:** `200 OK` with service status

## Configuration

Key configuration properties:

- `server.port`: Service port (default: 8081)
- `spring.kafka.bootstrap-servers`: Kafka broker addresses
- `spring.kafka.producer.*`: Producer configuration settings

## Running the Service

### Prerequisites

- Java 17 or higher
- Apache Kafka running on localhost:9092

### Using Maven

```bash
mvn clean install
mvn spring-boot:run
```

### Using Docker

```bash
docker build -t data-ingestion-service .
docker run -p 8081:8081 data-ingestion-service
```

## Monitoring

The service exposes health and metrics endpoints:

- `/actuator/health` - Service health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

## Error Handling

The service implements comprehensive error handling:

- **400 Bad Request**: Invalid input data
- **500 Internal Server Error**: Unexpected server errors

All errors are logged with appropriate levels for monitoring and debugging.

## Design Principles

This service follows software design best practices:

- **Single Responsibility**: Each class has one clear purpose
- **Dependency Inversion**: Services depend on abstractions, not concretions
- **Separation of Concerns**: Clear boundaries between layers
- **SOLID Principles**: Applied throughout the codebase
- **Clean Code**: Readable, maintainable, and well-documented code
