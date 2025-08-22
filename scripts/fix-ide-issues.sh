#!/bin/bash

# Vehicle Telemetry System - IDE Issue Resolver
# This script helps resolve common IDE issues and refreshes the project state

set -e

echo "🔧 Fixing IDE Issues for Vehicle Telemetry System..."
echo "=================================================="

# Navigate to project root
cd "$(dirname "$0")/.."

# Clean all Maven targets
echo "🧹 Cleaning Maven target directories..."
find . -name "target" -type d -exec rm -rf {} + 2>/dev/null || true

# Clean IDE-specific files
echo "🧹 Cleaning IDE cache files..."
find . -name ".vscode" -type d -exec rm -rf {} + 2>/dev/null || true
find . -name "*.iml" -type f -delete 2>/dev/null || true
find . -name ".idea" -type d -exec rm -rf {} + 2>/dev/null || true

# Rebuild all services
echo "🔨 Rebuilding all services..."
services=("data-ingestion-service" "data-processing-service" "api-gateway")

for service in "${services[@]}"; do
    echo "   Building $service..."
    cd "backend/$service"
    
    # Clean and compile
    mvn clean compile -q
    
    # Generate sources (helps with IDE recognition)
    mvn dependency:sources -q 2>/dev/null || true
    
    echo "   ✅ $service rebuilt successfully"
    cd "../.."
done

echo ""
echo "🎉 IDE Issue Resolution Complete!"
echo "=================================="
echo "📋 What was done:"
echo "  • Cleaned all Maven target directories"
echo "  • Removed IDE cache files"
echo "  • Rebuilt all services from scratch"
echo "  • Downloaded dependency sources"
echo ""
echo "💡 Next steps:"
echo "  1. Restart your IDE (VS Code/IntelliJ)"
echo "  2. Reload/refresh the project workspace"
echo "  3. Wait for IDE indexing to complete"
echo ""
echo "🔍 If issues persist:"
echo "  • Check Java version: java -version (should be 17+)"
echo "  • Verify Maven version: mvn -version (should be 3.6+)"
echo "  • Run: mvn clean install from project root"
echo "=================================="

