package com.vehicletelemetry.streaming.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vehicletelemetry.streaming.model.TelemetryData;
import com.vehicletelemetry.streaming.service.TelemetryStreamingService;

import reactor.core.publisher.Flux;

/**
 * TelemetryStreamingController - REST Controller for Real-time Telemetry
 * Streaming
 * 
 * This controller provides Server-Sent Events (SSE) endpoints for real-time
 * vehicle telemetry data streaming. It follows object-oriented principles
 * with proper dependency injection and clear separation of concerns.
 * 
 * Clean Architecture: This is part of the presentation layer.
 */
@RestController
@RequestMapping("/api/telemetry")
@CrossOrigin(origins = "http://localhost:4200")
public class TelemetryStreamingController {

    private final TelemetryStreamingService telemetryStreamingService;

    public TelemetryStreamingController(TelemetryStreamingService telemetryStreamingService) {
        this.telemetryStreamingService = telemetryStreamingService;
    }

    /**
     * SSE stream for specific vehicles (ESSENTIAL ENDPOINT)
     */
    @GetMapping(value = "/stream/vehicles", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TelemetryData> streamSpecificVehiclesTelemetry(@RequestParam List<String> vehicleIds) {
        return telemetryStreamingService.createSpecificVehiclesStream(vehicleIds);
    }

    /**
     * Health check (ESSENTIAL)
     */
    @GetMapping("/health")
    public String getServiceHealth() {
        return "UP";
    }
}
