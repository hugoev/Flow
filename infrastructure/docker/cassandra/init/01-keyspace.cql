-- Vehicle Telemetry System - Cassandra Initialization
-- Creates keyspace and tables for production use

-- Create keyspace with proper replication strategy
CREATE KEYSPACE IF NOT EXISTS vehicle_telemetry
WITH REPLICATION = {
    'class': 'SimpleStrategy',
    'replication_factor': 3
};

-- Use the keyspace
USE vehicle_telemetry;

-- Create main telemetry data table
CREATE TABLE IF NOT EXISTS vehicle_telemetry (
    vehicle_id text,
    timestamp timestamp,
    latitude double,
    longitude double,
    speed double,
    fuel_level double,
    engine_temp double,
    tire_pressure double,
    PRIMARY KEY (vehicle_id, timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC)
  AND comment = 'Main table for storing vehicle telemetry data'
  AND compaction = {
      'class': 'TimeWindowCompactionStrategy',
      'compaction_window_unit': 'HOURS',
      'compaction_window_size': 24
  };

-- Create secondary index for timestamp-based queries across vehicles
CREATE INDEX IF NOT EXISTS idx_vehicle_telemetry_timestamp 
ON vehicle_telemetry (timestamp);

-- Create materialized view for latest data per vehicle
CREATE MATERIALIZED VIEW IF NOT EXISTS vehicle_latest_telemetry AS
SELECT vehicle_id, timestamp, latitude, longitude, speed, fuel_level, engine_temp, tire_pressure
FROM vehicle_telemetry
WHERE vehicle_id IS NOT NULL 
  AND timestamp IS NOT NULL
PRIMARY KEY (vehicle_id, timestamp)
WITH CLUSTERING ORDER BY (timestamp DESC);

-- Create table for vehicle metadata
CREATE TABLE IF NOT EXISTS vehicle_metadata (
    vehicle_id text PRIMARY KEY,
    make text,
    model text,
    year int,
    vin text,
    created_at timestamp,
    last_seen timestamp
) WITH comment = 'Metadata about vehicles in the system';

-- Create table for system metrics
CREATE TABLE IF NOT EXISTS system_metrics (
    metric_name text,
    timestamp timestamp,
    value double,
    tags map<text, text>,
    PRIMARY KEY (metric_name, timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC)
  AND comment = 'System performance and health metrics';

-- Create table for alerts and notifications
CREATE TABLE IF NOT EXISTS vehicle_alerts (
    vehicle_id text,
    alert_id uuid,
    timestamp timestamp,
    alert_type text,
    severity text,
    message text,
    resolved boolean,
    resolved_at timestamp,
    PRIMARY KEY (vehicle_id, timestamp, alert_id)
) WITH CLUSTERING ORDER BY (timestamp DESC)
  AND comment = 'Vehicle alerts and notifications';

-- Insert initial test data
INSERT INTO vehicle_metadata (vehicle_id, make, model, year, vin, created_at)
VALUES ('VH001', 'Tesla', 'Model S', 2023, 'TEST123456789', toTimestamp(now()));

INSERT INTO vehicle_metadata (vehicle_id, make, model, year, vin, created_at)
VALUES ('VH002', 'BMW', 'i3', 2022, 'TEST987654321', toTimestamp(now()));

-- Grant permissions (if using authentication)
-- GRANT ALL ON KEYSPACE vehicle_telemetry TO vehicle_user;
