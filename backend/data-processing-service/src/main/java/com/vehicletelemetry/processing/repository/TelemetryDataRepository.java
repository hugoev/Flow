package com.vehicletelemetry.processing.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vehicletelemetry.processing.model.ProcessedTelemetryData;
import com.vehicletelemetry.processing.model.TelemetryDataKey;

/**
 * TelemetryDataRepository - Data Access Layer
 *
 * Repository interface for accessing processed telemetry data in Cassandra.
 * Provides efficient query methods optimized for time-series data access
 * patterns.
 *
 * Clean Architecture: This interface defines the contract for data access
 * while hiding the implementation details from the service layer.
 */
@Repository
public interface TelemetryDataRepository extends CassandraRepository<ProcessedTelemetryData, TelemetryDataKey> {

        /**
         * Find all telemetry data for a specific vehicle within a time range.
         * This query is optimized for time-series data retrieval.
         */
        @Query("SELECT * FROM vehicle_telemetry WHERE vehicle_id = :vehicleId " +
                        "AND timestamp >= :startTime AND timestamp <= :endTime ALLOW FILTERING")
        List<ProcessedTelemetryData> findByVehicleAndTimeRange(
                        @Param("vehicleId") String vehicleId,
                        @Param("startTime") Instant startTime,
                        @Param("endTime") Instant endTime);

        /**
         * Find the latest telemetry data for a specific vehicle.
         * Uses clustering order to get the most recent record efficiently.
         */
        @Query("SELECT * FROM vehicle_telemetry WHERE vehicle_id = :vehicleId " +
                        "ORDER BY timestamp DESC LIMIT 1")
        List<ProcessedTelemetryData> findLatestByVehicleId(@Param("vehicleId") String vehicleId);

        /**
         * Find all telemetry data for a specific vehicle.
         * Returns data sorted by timestamp in descending order (newest first).
         */
        @Query("SELECT * FROM vehicle_telemetry WHERE vehicle_id = :vehicleId")
        List<ProcessedTelemetryData> findByVehicleId(@Param("vehicleId") String vehicleId);

        /**
         * Count total records for a specific vehicle.
         * Useful for monitoring data volume per vehicle.
         */
        @Query("SELECT COUNT(*) FROM vehicle_telemetry WHERE vehicle_id = :vehicleId")
        Long countByVehicleId(@Param("vehicleId") String vehicleId);

        /**
         * Find telemetry data for a vehicle within a time range (method name matching
         * Spring Data conventions)
         */
        List<ProcessedTelemetryData> findByVehicleIdAndTimestampBetween(
                        String vehicleId, Instant startTime, Instant endTime);

        /**
         * Find top 10 most recent telemetry records for a specific vehicle
         */
        List<ProcessedTelemetryData> findTop10ByVehicleIdOrderByTimestampDesc(String vehicleId);
}
