-- Vehicle Telemetry System - Table Creation
-- This script creates the tables for storing telemetry data

USE vehicle_telemetry;

-- Main telemetry data table
-- Partitioned by vehicle_id for efficient vehicle-specific queries
-- Clustered by timestamp for time-series ordering
CREATE TABLE IF NOT EXISTS vehicle_telemetry (
    vehicle_id TEXT,
    timestamp TIMESTAMP,
    latitude DOUBLE,
    longitude DOUBLE,
    speed DOUBLE,
    fuel_level DOUBLE,
    engine_temp DOUBLE,
    tire_pressure DOUBLE,
    PRIMARY KEY (vehicle_id, timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);

-- Vehicle summary table for quick lookups
-- Stores latest data for each vehicle
CREATE TABLE IF NOT EXISTS vehicle_summary (
    vehicle_id TEXT PRIMARY KEY,
    last_seen TIMESTAMP,
    current_speed DOUBLE,
    current_fuel_level DOUBLE,
    current_engine_temp DOUBLE,
    total_records COUNTER,
    last_latitude DOUBLE,
    last_longitude DOUBLE
);

-- Vehicle alerts table for storing alerts and notifications
CREATE TABLE IF NOT EXISTS vehicle_alerts (
    vehicle_id TEXT,
    alert_id UUID,
    timestamp TIMESTAMP,
    alert_type TEXT,
    severity TEXT,
    message TEXT,
    resolved BOOLEAN,
    PRIMARY KEY (vehicle_id, timestamp, alert_id)
) WITH CLUSTERING ORDER BY (timestamp DESC);

-- Create indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_vehicle_telemetry_timestamp 
ON vehicle_telemetry (timestamp);

CREATE INDEX IF NOT EXISTS idx_vehicle_alerts_type 
ON vehicle_alerts (alert_type);

CREATE INDEX IF NOT EXISTS idx_vehicle_alerts_resolved 
ON vehicle_alerts (resolved);


