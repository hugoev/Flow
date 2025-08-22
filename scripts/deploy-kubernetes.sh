#!/bin/bash

# Vehicle Telemetry System - Kubernetes Deployment Script
# Deploys the complete system to a Kubernetes cluster

set -e

echo "🚀 Deploying Vehicle Telemetry System to Kubernetes..."
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2

    if [ "$status" = "success" ]; then
        echo -e "${GREEN}✅ $message${NC}"
    elif [ "$status" = "info" ]; then
        echo -e "${BLUE}ℹ️  $message${NC}"
    elif [ "$status" = "warning" ]; then
        echo -e "${YELLOW}⚠️  $message${NC}"
    else
        echo -e "${RED}❌ $message${NC}"
    fi
}

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    print_status "error" "kubectl is not installed or not in PATH"
    exit 1
fi

# Check if we can connect to the cluster
if ! kubectl cluster-info &> /dev/null; then
    print_status "error" "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
    exit 1
fi

print_status "info" "Connected to Kubernetes cluster: $(kubectl config current-context)"

# Navigate to infrastructure directory
cd "$(dirname "$0")/../infrastructure/kubernetes"

# Deploy in order with proper dependencies
echo ""
print_status "info" "Step 1: Creating namespace and basic configuration..."
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f storage.yaml

print_status "success" "Namespace and configuration created"

echo ""
print_status "info" "Step 2: Deploying Kafka cluster..."
kubectl apply -f kafka-deployment.yaml

# Wait for Kafka to be ready
echo "⏳ Waiting for Kafka cluster to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/kafka -n vehicle-telemetry
print_status "success" "Kafka cluster is ready"

echo ""
print_status "info" "Step 3: Deploying Cassandra cluster..."
kubectl apply -f cassandra-deployment.yaml

# Wait for Cassandra to be ready
echo "⏳ Waiting for Cassandra cluster to be ready..."
kubectl wait --for=condition=ready --timeout=600s pod -l app=cassandra -n vehicle-telemetry
print_status "success" "Cassandra cluster is ready"

echo ""
print_status "info" "Step 4: Deploying application services..."
kubectl apply -f microservices-deployment.yaml

# Wait for services to be ready
echo "⏳ Waiting for application services to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/data-ingestion-service -n vehicle-telemetry
kubectl wait --for=condition=available --timeout=300s deployment/data-processing-service -n vehicle-telemetry
kubectl wait --for=condition=available --timeout=300s deployment/api-gateway-service -n vehicle-telemetry

print_status "success" "Application services are ready"

echo ""
print_status "info" "Step 5: Deploying monitoring stack..."
kubectl apply -f monitoring.yaml

# Wait for monitoring services
echo "⏳ Waiting for monitoring services to be ready..."
kubectl wait --for=condition=available --timeout=180s deployment/kafka-ui -n vehicle-telemetry
kubectl wait --for=condition=available --timeout=180s deployment/cassandra-web -n vehicle-telemetry

print_status "success" "Monitoring stack is ready"

echo ""
echo "🎉 Deployment Complete!"
echo "======================"

# Get service information
echo ""
print_status "info" "Service Information:"

# Get LoadBalancer IPs/URLs
INGESTION_IP=$(kubectl get service data-ingestion-service -n vehicle-telemetry -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending")
GATEWAY_IP=$(kubectl get service api-gateway-service -n vehicle-telemetry -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending")
KAFKA_UI_IP=$(kubectl get service kafka-ui-service -n vehicle-telemetry -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending")
CASSANDRA_WEB_IP=$(kubectl get service cassandra-web-service -n vehicle-telemetry -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending")

echo "📊 Application Services:"
echo "  • Data Ingestion Service: http://${INGESTION_IP}:8081/api/telemetry"
echo "  • API Gateway Service:    http://${GATEWAY_IP}:8080/api/telemetry"
echo ""
echo "🔍 Monitoring Services:"
echo "  • Kafka UI:              http://${KAFKA_UI_IP}:8083"
echo "  • Cassandra Web:         http://${CASSANDRA_WEB_IP}:3000"
echo ""
echo "📋 Useful Commands:"
echo "  • Check pod status:      kubectl get pods -n vehicle-telemetry"
echo "  • Check service status:  kubectl get services -n vehicle-telemetry"
echo "  • View logs:             kubectl logs -f deployment/[service-name] -n vehicle-telemetry"
echo "  • Scale service:         kubectl scale deployment/[service-name] --replicas=5 -n vehicle-telemetry"
echo ""
echo "🧪 Test the deployment:"
echo "  curl -X POST http://${INGESTION_IP}:8081/api/telemetry/ingest \\"
echo '    -H "Content-Type: application/json" \'
echo '    -d '\''{"vehicleId": "K8S001", "timestamp": "2023-12-07T10:30:00Z", "latitude": 40.7128, "longitude": -74.0060, "speed": 65.5, "fuelLevel": 85.2, "engineTemp": 90.5, "tirePressure": 32.0}'\'''
echo ""
echo "🛑 To remove the deployment:"
echo "   kubectl delete namespace vehicle-telemetry"
echo "=================================================="

# Show current status
echo ""
print_status "info" "Current Deployment Status:"
kubectl get pods -n vehicle-telemetry -o wide
