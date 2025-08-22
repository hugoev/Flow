package com.vehicletelemetry.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Data Ingestion Service - Main Application Class
 *
 * This service is responsible for:
 * - Receiving telemetry data from vehicles via REST API
 * - Validating and transforming incoming data
 * - Publishing data to Kafka for further processing
 *
 * Clean Architecture: This is the entry point that bootstraps the application
 * and wires together all components through Spring's dependency injection.
 */
@SpringBootApplication
public class DataIngestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataIngestionApplication.class, args);
    }
}
