# Vehicle Telemetry System

A scalable, real-time IoT platform designed to process, store, and visualize vehicle sensor data using modern microservices architecture.

## Overview

This project demonstrates a **production-ready, enterprise-grade** vehicle telemetry system built with **Clean Architecture** principles and **SOLID design patterns**. The system follows **Infrastructure as Code** practices with comprehensive documentation and monitoring.

### Key Components

- **Data Ingestion Service** (Port 8081): REST API for receiving telemetry data with validation and Kafka publishing
- **Data Processing Service** (Port 8082): Kafka consumer for processing and storing data in Cassandra
- **API Gateway Service** (Port 8080): Unified API with circuit breakers, caching, and resilience patterns
- **Infrastructure Layer**: Complete Docker and Kubernetes configurations for all environments

### Architecture Highlights

✅ **Clean Architecture** - Proper separation of Domain, Service, Infrastructure, and Presentation layers
✅ **SOLID Principles** - Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
✅ **Microservices** - Loosely coupled services with clear boundaries and responsibilities
✅ **Event-Driven** - Asynchronous communication via Apache Kafka
✅ **Infrastructure as Code** - Version-controlled deployment configurations
✅ **Production-Ready** - Comprehensive monitoring, health checks, and error handling

## Architecture

The system follows clean architecture principles with clear separation of concerns:

```
┌─────────────────┐    HTTP     ┌──────────────────┐
│   Frontend      │────────────▶│  API Gateway     │
│   (Angular)     │             │  (Port 8080)     │
└─────────────────┘             └──────────────────┘
                                          │
                                          │ Feign Client
                                          ▼
┌─────────────────┐    Kafka    ┌──────────────────┐
│ Data Ingestion  │────────────▶│ Data Processing  │
│ (Port 8081)     │             │ (Port 8082)      │
└─────────────────┘             └──────────────────┘
                                          │
                                          │
                                          ▼
                                    ┌──────────────────┐
                                    │   Cassandra DB   │
                                    │   (Port 9042)    │
                                    └──────────────────┘
```

### Design Principles Applied

- **Single Responsibility**: Each service has one clear purpose
- **Dependency Inversion**: Services depend on abstractions, not concretions
- **Separation of Concerns**: Clear boundaries between layers
- **SOLID Principles**: Applied throughout the codebase
- **Clean Code**: Readable, maintainable, and well-documented code
- **Microservices**: Loosely coupled, independently deployable services

## Prerequisites

- Java 17 or higher
- Apache Kafka 2.8+
- Apache Cassandra 4.0+
- Node.js 16+ (for frontend)
- Maven 3.6+

## Quick Start

### 1. Start Infrastructure Services

**Apache Kafka:**

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka Server
bin/kafka-server-start.sh config/server.properties

# Create topic
bin/kafka-topics.sh --create --topic vehicle-telemetry --bootstrap-server localhost:9092
```

**Apache Cassandra:**

```bash
# Start Cassandra
bin/cassandra

# Verify it's running
bin/cqlsh
```

### 2. Build and Start Backend Services

**Data Ingestion Service:**

```bash
cd backend/data-ingestion-service
mvn clean install
mvn spring-boot:run
```

**Data Processing Service:**

```bash
cd backend/data-processing-service
mvn clean install
mvn spring-boot:run
```

**API Gateway Service:**

```bash
cd backend/api-gateway
mvn clean install
mvn spring-boot:run
```

### 3. Test the System

**Send telemetry data:**

```bash
curl -X POST http://localhost:8081/api/telemetry/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": "VH001",
    "timestamp": "2023-12-07T10:30:00Z",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "speed": 65.5,
    "fuelLevel": 85.2,
    "engineTemp": 90.5,
    "tirePressure": 32.0
  }'
```

**Retrieve telemetry data:**

```bash
curl "http://localhost:8080/api/telemetry/vehicles/VH001?startTime=2023-12-07T00:00:00Z&endTime=2023-12-07T23:59:59Z"
```

## API Documentation

Once the API Gateway is running, you can access:

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/api-docs

### Key Endpoints

| Endpoint                              | Method | Description                             |
| ------------------------------------- | ------ | --------------------------------------- |
| `/api/telemetry/ingest`               | POST   | Ingest single telemetry data point      |
| `/api/telemetry/ingest/batch`         | POST   | Ingest multiple telemetry data points   |
| `/api/telemetry/vehicles/{id}`        | GET    | Get telemetry data for specific vehicle |
| `/api/telemetry/vehicles/{id}/latest` | GET    | Get latest telemetry data for vehicle   |
| `/api/telemetry/vehicles`             | GET    | Get summary of all vehicles             |

## Configuration

Each service can be configured via `application.properties`:

### Data Ingestion Service (Port 8081)

- Kafka producer settings
- Validation rules
- Thread pool configuration

### Data Processing Service (Port 8082)

- Kafka consumer settings
- Cassandra connection settings
- Processing batch size

### API Gateway Service (Port 8080)

- Service discovery URLs
- Circuit breaker settings
- Caching configuration

## Monitoring

Each service exposes health and metrics endpoints:

- **Health**: `http://localhost:{port}/actuator/health`
- **Metrics**: `http://localhost:{port}/actuator/metrics`
- **Info**: `http://localhost:{port}/actuator/info`

## Development

### Adding New Telemetry Fields

1. Update `TelemetryData.java` in the ingestion service
2. Update `ProcessedTelemetryData.java` in the processing service
3. Update `TelemetryDataResponse.java` in the API gateway
4. Update database schema if needed

### Adding New Services

1. Create new Maven module following existing structure
2. Implement clean architecture layers
3. Configure service discovery in API Gateway
4. Add Docker configuration

## Deployment

### Docker (Recommended)

```bash
# Development environment (lightweight)
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d

# Production environment (full stack)
cd infrastructure/docker
docker-compose -f docker-compose.yml up -d
```

### Kubernetes

```bash
# Deploy to Kubernetes cluster
./scripts/deploy-kubernetes.sh

# Or manually:
cd infrastructure/kubernetes
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f storage.yaml
kubectl apply -f kafka-deployment.yaml
kubectl apply -f cassandra-deployment.yaml
kubectl apply -f microservices-deployment.yaml
kubectl apply -f monitoring.yaml
```

## Performance Considerations

- **Kafka**: Configured for high-throughput with appropriate batch sizes
- **Cassandra**: Optimized for time-series queries with proper partitioning
- **Services**: Horizontal scaling support with stateless design
- **API Gateway**: Circuit breakers and retries for fault tolerance

## Security

- Input validation on all endpoints
- Service-to-service authentication (can be added)
- CORS configuration for frontend integration
- Rate limiting (can be implemented via API Gateway)

## Contributing

1. Follow the established clean architecture patterns
2. Add tests for new functionality
3. Update documentation
4. Ensure all services build and run successfully

## Troubleshooting

### Common Issues

**Kafka Connection Issues:**

- Ensure Kafka is running on localhost:9092
- Check topic exists: `kafka-topics.sh --list --bootstrap-server localhost:9092`

**Cassandra Connection Issues:**

- Ensure Cassandra is running on localhost:9042
- Verify keyspace exists: `DESCRIBE KEYSPACES;`

**Service Communication Issues:**

- Check service ports are not in use
- Verify Feign client URLs in API Gateway configuration

### Logs

Check service logs for detailed error information:

```bash
# View service logs
tail -f logs/application.log

# Enable debug logging
logging.level.com.vehicletelemetry=DEBUG
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
