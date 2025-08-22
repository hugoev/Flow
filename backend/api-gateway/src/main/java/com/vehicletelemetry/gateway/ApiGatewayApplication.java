package com.vehicletelemetry.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * API Gateway Service - Main Application Class
 *
 * This service provides a unified REST API for accessing telemetry data
 * and acts as the main entry point for frontend applications.
 *
 * Features:
 * - REST API endpoints for telemetry data retrieval
 * - Service-to-service communication via Feign clients
 * - Circuit breaker patterns for resilience
 * - Request routing and aggregation
 * - OpenAPI documentation
 *
 * Clean Architecture: This is the entry point that bootstraps the application
 * and wires together all components through Spring's dependency injection.
 */
@SpringBootApplication
@EnableFeignClients
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
