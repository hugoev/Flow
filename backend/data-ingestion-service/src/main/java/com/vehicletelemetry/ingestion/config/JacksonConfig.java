package com.vehicletelemetry.ingestion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Jackson Configuration for Data Ingestion Service
 * 
 * This configuration ensures proper serialization of Java 8 time types
 * like Instant, LocalDateTime, etc. The JSR310 module is required for
 * handling these types in JSON serialization/deserialization.
 */
@Configuration
public class JacksonConfig {

    /**
     * Configure ObjectMapper with JSR310 module for Java 8 time support
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register the JSR310 module for Java 8 time types
        mapper.registerModule(new JavaTimeModule());

        // Configure to write dates as ISO strings instead of timestamps
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}

