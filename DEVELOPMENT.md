# Vehicle Telemetry System - Development Guide

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 17+ (for local development)
- Node.js 18+ (for local development)
- Maven 3.6+ (for local development)

### Start Development Environment

```bash
# Start the full development stack with hot reloading
./scripts/start-dev.sh
```

This will start:

- **Infrastructure**: Kafka, Cassandra, Redis, MQTT (Mosquitto)
- **Backend Services**: All 3 microservices with hot reloading
- **Frontend**: Angular dashboard with live reload
- **Monitoring**: Kafka UI, Redis Commander

## 🔧 Development Features

### Hot Reloading

- **Backend**: Spring Boot DevTools automatically restarts services on code changes
- **Frontend**: Angular live reload updates browser automatically
- **Volume Mounts**: Source code is mounted for instant changes

### Development Tools

- **Kafka UI**: http://localhost:8084 - Monitor Kafka topics and messages
- **Redis Commander**: http://localhost:8085 - Redis data browser
- **Health Checks**: All services expose health endpoints
- **Debug Logging**: Enhanced logging for development

## 📋 Service URLs

| Service             | URL                       | Description                        |
| ------------------- | ------------------------- | ---------------------------------- |
| Frontend Dashboard  | http://localhost:4200     | Angular dashboard with live reload |
| Data Ingestion API  | http://localhost:8081/api | MQTT to Kafka bridge               |
| Data Processing API | http://localhost:8082/api | Kafka to Cassandra processor       |
| Telemetry Streaming | http://localhost:8083/api | Real-time streaming service        |
| Kafka UI            | http://localhost:8084     | Kafka monitoring interface         |
| Redis Commander     | http://localhost:8085     | Redis data browser                 |

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   MQTT Broker   │    │   Kafka UI      │
│   (Angular)     │    │   (Mosquitto)   │    │   (Monitoring)  │
│   Port: 4200    │    │   Port: 1883    │    │   Port: 8084    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Data Ingestion  │    │      Kafka      │    │   Redis         │
│   Service       │◄───┤   (Message      │    │   (Cache)       │
│   Port: 8081    │    │    Broker)     │    │   Port: 6379    │
└─────────────────┘    │   Port: 9092    │    └─────────────────┘
         │             └─────────────────┘             │
         │                       │                   │
         ▼                       ▼                   ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Data Processing │    │   Cassandra     │    │ Telemetry       │
│   Service       │◄───┤   (Database)    │    │ Streaming       │
│   Port: 8082    │    │   Port: 9042    │    │ Service         │
└─────────────────┘    └─────────────────┘    │ Port: 8083      │
                                             └─────────────────┘
```

## 🛠️ Development Workflow

### 1. Code Changes

- **Backend**: Edit Java files in `backend/*/src/` - changes auto-reload
- **Frontend**: Edit TypeScript/HTML files in `frontend/src/` - browser auto-refreshes
- **Configuration**: Edit `.properties` files - services restart automatically

### 2. Debugging

- **Backend**: Use IDE debugger on local ports (8081, 8082, 8083)
- **Frontend**: Use browser dev tools
- **Logs**: `docker-compose -f docker-compose.dev.yml logs -f [service-name]`

### 3. Testing

- **Unit Tests**: Run locally with Maven/Node
- **Integration Tests**: Use Docker services
- **API Testing**: Use health endpoints and monitoring tools

## 📁 Project Structure

```
Flow/
├── backend/
│   ├── data-ingestion-service/     # MQTT → Kafka
│   ├── data-processing-service/     # Kafka → Cassandra
│   └── telemetry-streaming-service/ # Real-time streaming
├── frontend/                       # Angular dashboard
├── infrastructure/
│   └── docker/
│       ├── docker-compose.yml      # Production
│       ├── docker-compose.dev.yml  # Development
│       └── cassandra/init/         # DB initialization
└── scripts/
    └── start-dev.sh               # Development startup
```

## 🔍 Monitoring & Debugging

### Health Checks

```bash
# Check all services
curl http://localhost:8081/api/telemetry/health    # Data Ingestion
curl http://localhost:8082/api/processing/health    # Data Processing
curl http://localhost:8083/api/telemetry/health    # Telemetry Streaming
```

### View Logs

```bash
# All services
docker-compose -f docker-compose.dev.yml logs -f

# Specific service
docker-compose -f docker-compose.dev.yml logs -f data-ingestion-service
docker-compose -f docker-compose.dev.yml logs -f frontend-dashboard
```

### Database Access

```bash
# Cassandra
docker-compose -f docker-compose.dev.yml exec cassandra cqlsh

# Redis
docker-compose -f docker-compose.dev.yml exec redis redis-cli
```

## 🚨 Troubleshooting

### Common Issues

1. **Port Conflicts**

   ```bash
   # Check what's using ports
   lsof -i :8081
   lsof -i :8082
   lsof -i :8083
   lsof -i :4200
   ```

2. **Service Won't Start**

   ```bash
   # Check service logs
   docker-compose -f docker-compose.dev.yml logs [service-name]

   # Restart specific service
   docker-compose -f docker-compose.dev.yml restart [service-name]
   ```

3. **Hot Reload Not Working**

   - Ensure volume mounts are correct
   - Check Spring Boot DevTools is enabled
   - Verify file permissions

4. **Database Connection Issues**

   ```bash
   # Check if Cassandra is running
   docker-compose -f docker-compose.dev.yml exec cassandra cqlsh -e "describe keyspaces"

   # Check Redis
   docker-compose -f docker-compose.dev.yml exec redis redis-cli ping
   ```

### Reset Development Environment

```bash
# Stop all services
docker-compose -f docker-compose.dev.yml down

# Remove volumes (WARNING: deletes all data)
docker-compose -f docker-compose.dev.yml down -v

# Rebuild and start
docker-compose -f docker-compose.dev.yml build --no-cache
docker-compose -f docker-compose.dev.yml up -d
```

## 🎯 Development Tips

### Backend Development

- Use Spring Boot DevTools for hot reloading
- Enable debug logging in `application-dev.properties`
- Use IDE integration for debugging
- Test with real MQTT messages

### Frontend Development

- Angular CLI provides live reload
- Use browser dev tools for debugging
- Test with real backend data
- Use Angular Material for UI components

### Full-Stack Testing

- Send MQTT messages to test data flow
- Monitor Kafka topics
- Check Cassandra for data persistence
- Verify Redis caching

## 📚 Additional Resources

- [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools)
- [Angular Live Reload](https://angular.io/cli/serve)
- [Docker Compose Development](https://docs.docker.com/compose/overview/)
- [Kafka UI Documentation](https://docs.kafka-ui.provectus.io/)
- [Redis Commander](https://github.com/joeferner/redis-commander)

---

**Happy Coding! 🚀**

For production deployment, see [PRODUCTION_ARCHITECTURE.md](docs/architecture/PRODUCTION_ARCHITECTURE.md)
