package com.vehicletelemetry.processing.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

/**
 * TelemetryDataKey - Cassandra Composite Primary Key
 *
 * Defines the primary key structure for telemetry data storage.
 * Partitioned by vehicle_id for efficient data distribution and
 * clustered by timestamp for time-series ordering within each partition.
 */
@PrimaryKeyClass
public class TelemetryDataKey implements Serializable {

    @PrimaryKeyColumn(name = "vehicle_id", type = PrimaryKeyType.PARTITIONED)
    private String vehicleId;

    @PrimaryKeyColumn(name = "timestamp", type = PrimaryKeyType.CLUSTERED)
    private Instant timestamp;

    // Default constructor required by Cassandra
    public TelemetryDataKey() {
    }

    public TelemetryDataKey(String vehicleId, Instant timestamp) {
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TelemetryDataKey that = (TelemetryDataKey) o;
        return Objects.equals(vehicleId, that.vehicleId) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, timestamp);
    }

    @Override
    public String toString() {
        return "TelemetryDataKey{" +
                "vehicleId='" + vehicleId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
