package com.vehicletelemetry.streaming;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Telemetry Streaming Service - Main Application
 * 
 * Real-time telemetry data streaming service for IoT platforms.
 * Provides Server-Sent Events (SSE) for real-time data distribution.
 * 
 * This is what experienced engineers actually use in production IoT systems.
 * 
 * Clean Architecture: This is the entry point that bootstraps the application
 * and wires together all components through Spring's dependency injection.
 */
@SpringBootApplication
public class TelemetryStreamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelemetryStreamingApplication.class, args);
    }

    /**
     * CORS Configuration for frontend communication
     * Allows Angular frontend (localhost:4200) to communicate with the streaming
     * service
     */
    @Bean
    public WebMvcConfigurer corsConfigurationProvider() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry corsRegistry) {
                corsRegistry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:4200", "http://127.0.0.1:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

    /**
     * CORS Configuration Source for additional CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSourceProvider() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "http://127.0.0.1:*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/api/**", corsConfiguration);
        return corsConfigurationSource;
    }
}
