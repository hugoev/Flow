package com.vehicletelemetry.processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Data Processing Service - Main Application Class
 *
 * This service is responsible for:
 * - Consuming telemetry data from Kafka topics
 * - Processing and aggregating the data
 * - Storing processed data in Cassandra for efficient querying
 * - Providing real-time analytics capabilities
 *
 * Clean Architecture: This is the entry point that bootstraps the application
 * and wires together all components through Spring's dependency injection.
 */
@SpringBootApplication
public class DataProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataProcessingApplication.class, args);
    }
}
