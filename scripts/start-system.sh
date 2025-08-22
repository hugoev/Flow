#!/bin/bash

# Vehicle Telemetry System - Start Script
# This script helps you start the entire system with all dependencies

set -e

echo "🚗 Starting Vehicle Telemetry System..."
echo "=========================================="

# Function to check if a port is in use
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null; then
        echo "❌ Port $1 is already in use. Please stop the service using this port first."
        exit 1
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1

    echo "⏳ Waiting for $service_name to be ready..."

    while [ $attempt -le $max_attempts ]; do
        if curl -s $url > /dev/null 2>&1; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        echo "   Attempt $attempt/$max_attempts..."
        sleep 2
        ((attempt++))
    done

    echo "❌ $service_name failed to start within expected time"
    exit 1
}

# Check if required ports are available
echo "🔍 Checking port availability..."
check_port 2181  # Zookeeper
check_port 9092  # Kafka
check_port 9042  # Cassandra
check_port 8080  # API Gateway
check_port 8081  # Data Ingestion
check_port 8082  # Data Processing

# Start infrastructure services
echo "🐳 Starting infrastructure services with Docker Compose..."
cd infrastructure/docker
docker-compose -f docker-compose.yml up -d zookeeper kafka cassandra
cd ../..

# Wait for Kafka to be ready
wait_for_service "Kafka" "http://localhost:9092"

# Create Kafka topic
echo "📝 Creating Kafka topic..."
docker exec kafka kafka-topics --create --topic vehicle-telemetry --bootstrap-server localhost:9092 --if-not-exists

# Wait for Cassandra to be ready
wait_for_service "Cassandra" "http://localhost:9042"

# Start backend services
echo "🔧 Starting backend services..."
cd infrastructure/docker
docker-compose -f docker-compose.yml up -d data-ingestion-service data-processing-service api-gateway-service
cd ../..

# Wait for services to be ready
wait_for_service "Data Ingestion Service" "http://localhost:8081/api/telemetry/health"
wait_for_service "Data Processing Service" "http://localhost:8082/api/processing/health"
wait_for_service "API Gateway Service" "http://localhost:8080/api/telemetry/health"

echo ""
echo "🎉 All services started successfully!"
echo "=========================================="
echo "📊 Service URLs:"
echo "  • API Gateway:     http://localhost:8080/api"
echo "  • Swagger UI:      http://localhost:8080/api/swagger-ui.html"
echo "  • Kafka UI:        http://localhost:8083"
echo "  • Cassandra Web:   http://localhost:3000"
echo ""
echo "📝 Test Commands:"
echo "  • Send telemetry:   curl -X POST http://localhost:8081/api/telemetry/ingest \\"
echo '                      -H "Content-Type: application/json" \\'
echo '                      -d '\''{"vehicleId": "VH001", "timestamp": "2023-12-07T10:30:00Z", "latitude": 40.7128, "longitude": -74.0060, "speed": 65.5, "fuelLevel": 85.2, "engineTemp": 90.5, "tirePressure": 32.0}'\'''
echo ""
echo "  • Get telemetry:    curl \"http://localhost:8080/api/telemetry/vehicles/VH001?startTime=2023-12-07T00:00:00Z&endTime=2023-12-07T23:59:59Z\""
echo ""
echo "🛑 To stop all services:"
echo "   docker-compose down"
echo "=========================================="

