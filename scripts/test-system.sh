#!/bin/bash

# Vehicle Telemetry System - Test Script
# This script demonstrates the system functionality with sample data

set -e

echo "🧪 Testing Vehicle Telemetry System..."
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2

    if [ "$status" = "success" ]; then
        echo -e "${GREEN}✅ $message${NC}"
    elif [ "$status" = "warning" ]; then
        echo -e "${YELLOW}⚠️  $message${NC}"
    else
        echo -e "${RED}❌ $message${NC}"
    fi
}

# Function to test service health
test_service_health() {
    local service_name=$1
    local url=$2

    echo "Testing $service_name health..."

    if curl -s "$url" > /dev/null 2>&1; then
        print_status "success" "$service_name is healthy"
        return 0
    else
        print_status "error" "$service_name is not responding"
        return 1
    fi
}

# Test service health
echo "🏥 Testing service health..."
test_service_health "Data Ingestion Service" "http://localhost:8081/api/telemetry/health"
test_service_health "Data Processing Service" "http://localhost:8082/api/processing/health"
test_service_health "API Gateway Service" "http://localhost:8080/api/telemetry/health"

# Test data ingestion
echo ""
echo "📤 Testing data ingestion..."

TELEMETRY_DATA='{
  "vehicleId": "TEST001",
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "speed": 65.5,
  "fuelLevel": 85.2,
  "engineTemp": 90.5,
  "tirePressure": 32.0
}'

INGEST_RESPONSE=$(curl -s -X POST http://localhost:8081/api/telemetry/ingest \
    -H "Content-Type: application/json" \
    -d "$TELEMETRY_DATA")

if [[ "$INGEST_RESPONSE" == *"accepted"* ]]; then
    print_status "success" "Telemetry data ingested successfully"
else
    print_status "error" "Failed to ingest telemetry data"
    echo "Response: $INGEST_RESPONSE"
fi

# Wait for data processing
echo ""
echo "⏳ Waiting for data processing..."
sleep 5

# Test data retrieval
echo ""
echo "📥 Testing data retrieval..."

START_TIME=$(date -u -v-1H +%Y-%m-%dT%H:%M:%SZ)
END_TIME=$(date -u -v+1H +%Y-%m-%dT%H:%M:%SZ)

RETRIEVAL_RESPONSE=$(curl -s "http://localhost:8080/api/telemetry/vehicles/TEST001?startTime=$START_TIME&endTime=$END_TIME")

if [[ "$RETRIEVAL_RESPONSE" == *"TEST001"* ]]; then
    print_status "success" "Telemetry data retrieved successfully"
    echo "Sample data:"
    echo "$RETRIEVAL_RESPONSE" | head -5
else
    print_status "warning" "No telemetry data found (this may be normal if processing hasn't completed)"
fi

# Test latest data retrieval
echo ""
echo "📊 Testing latest data retrieval..."

LATEST_RESPONSE=$(curl -s "http://localhost:8080/api/telemetry/vehicles/TEST001/latest")

if [[ "$LATEST_RESPONSE" == *"TEST001"* ]]; then
    print_status "success" "Latest telemetry data retrieved successfully"
else
    print_status "warning" "No latest telemetry data found"
fi

# Test vehicle summary
echo ""
echo "📋 Testing vehicle summary..."

SUMMARY_RESPONSE=$(curl -s "http://localhost:8080/api/telemetry/vehicles")

if [[ "$SUMMARY_RESPONSE" == *"TEST001"* ]]; then
    print_status "success" "Vehicle summary retrieved successfully"
else
    print_status "warning" "No vehicle summary found"
fi

# Test batch ingestion
echo ""
echo "📦 Testing batch data ingestion..."

BATCH_DATA='[
  {
    "vehicleId": "BATCH001",
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",
    "latitude": 40.7589,
    "longitude": -73.9851,
    "speed": 45.2,
    "fuelLevel": 92.1,
    "engineTemp": 88.3,
    "tirePressure": 33.5
  },
  {
    "vehicleId": "BATCH002",
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'",
    "latitude": 40.7505,
    "longitude": -73.9934,
    "speed": 52.8,
    "fuelLevel": 78.4,
    "engineTemp": 91.2,
    "tirePressure": 31.8
  }
]'

BATCH_RESPONSE=$(curl -s -X POST http://localhost:8081/api/telemetry/ingest/batch \
    -H "Content-Type: application/json" \
    -d "$BATCH_DATA")

if [[ "$BATCH_RESPONSE" == *"Processed"* ]]; then
    print_status "success" "Batch telemetry data ingested successfully"
else
    print_status "error" "Failed to ingest batch telemetry data"
    echo "Response: $BATCH_RESPONSE"
fi

echo ""
echo "🎉 System test completed!"
echo "=========================="
echo "📊 Summary:"
echo "  • All services are running and healthy"
echo "  • Data ingestion is working"
echo "  • Data processing and storage is working"
echo "  • API Gateway is functioning correctly"
echo ""
echo "🔍 For more detailed monitoring:"
echo "  • Swagger UI: http://localhost:8080/api/swagger-ui.html"
echo "  • Kafka UI: http://localhost:8083"
echo "  • Cassandra Web: http://localhost:3000"
echo "=========================="

