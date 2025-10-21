#!/bin/bash

# Vehicle Telemetry System - Development Startup Script
# Starts the full development environment with hot reloading

set -e

echo "🚀 Starting Vehicle Telemetry System - Development Environment"
echo "=============================================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Navigate to the docker directory
cd "$(dirname "$0")/../infrastructure/docker"

echo "📦 Building development images..."
docker-compose -f docker-compose.dev.yml build

echo "🔄 Starting development services..."
docker-compose -f docker-compose.dev.yml up -d

echo "⏳ Waiting for services to be ready..."

# Wait for Cassandra
echo "📊 Waiting for Cassandra..."
until docker-compose -f docker-compose.dev.yml exec cassandra cqlsh -e "describe keyspaces" > /dev/null 2>&1; do
    echo "   Cassandra is starting up..."
    sleep 5
done
echo "✅ Cassandra is ready!"

# Wait for Kafka
echo "📨 Waiting for Kafka..."
until docker-compose -f docker-compose.dev.yml exec kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; do
    echo "   Kafka is starting up..."
    sleep 5
done
echo "✅ Kafka is ready!"

# Wait for Redis
echo "🗄️  Waiting for Redis..."
until docker-compose -f docker-compose.dev.yml exec redis redis-cli ping > /dev/null 2>&1; do
    echo "   Redis is starting up..."
    sleep 2
done
echo "✅ Redis is ready!"

echo ""
echo "🎉 Development environment is ready!"
echo ""
echo "📋 Service URLs:"
echo "   • Frontend Dashboard:     http://localhost:4200"
echo "   • Data Ingestion API:     http://localhost:8081/api"
echo "   • Data Processing API:    http://localhost:8082/api"
echo "   • Telemetry Streaming:    http://localhost:8083/api"
echo "   • Kafka UI:              http://localhost:8084"
echo "   • Redis Commander:       http://localhost:8085"
echo ""
echo "🔧 Development Tools:"
echo "   • Hot reloading enabled for all services"
echo "   • Debug logging enabled"
echo "   • Spring Boot DevTools active"
echo "   • Angular live reload active"
echo ""
echo "📊 Health Checks:"
echo "   • Data Ingestion:         http://localhost:8081/api/telemetry/health"
echo "   • Data Processing:        http://localhost:8082/api/processing/health"
echo "   • Telemetry Streaming:    http://localhost:8083/api/telemetry/health"
echo ""
echo "🛑 To stop the development environment:"
echo "   docker-compose -f docker-compose.dev.yml down"
echo ""
echo "📝 To view logs:"
echo "   docker-compose -f docker-compose.dev.yml logs -f [service-name]"
echo ""
echo "Happy coding! 🚀"
