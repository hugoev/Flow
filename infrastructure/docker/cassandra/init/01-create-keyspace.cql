-- Vehicle Telemetry System - Cassandra Keyspace Creation
-- This script creates the keyspace for the vehicle telemetry system

-- Create keyspace with simple replication strategy for development
CREATE KEYSPACE IF NOT EXISTS vehicle_telemetry
WITH REPLICATION = {
    'class': 'SimpleStrategy',
    'replication_factor': 1
};

-- Use the keyspace
USE vehicle_telemetry;


