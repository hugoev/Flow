-- Vehicle Telemetry System - Sample Data Insertion
-- This script inserts sample data for testing and demonstration

USE vehicle_telemetry;

-- Insert sample telemetry data for testing
INSERT INTO vehicle_telemetry (vehicle_id, timestamp, latitude, longitude, speed, fuel_level, engine_temp, tire_pressure) 
VALUES ('VH001', toTimestamp(now()), 40.7128, -74.0060, 65.5, 85.2, 90.5, 32.0);

INSERT INTO vehicle_telemetry (vehicle_id, timestamp, latitude, longitude, speed, fuel_level, engine_temp, tire_pressure) 
VALUES ('VH001', toTimestamp(now()) - 300000, 40.7130, -74.0058, 70.2, 84.8, 92.1, 31.8);

INSERT INTO vehicle_telemetry (vehicle_id, timestamp, latitude, longitude, speed, fuel_level, engine_temp, tire_pressure) 
VALUES ('VH002', toTimestamp(now()), 40.7589, -73.9851, 45.3, 92.5, 88.7, 33.2);

INSERT INTO vehicle_telemetry (vehicle_id, timestamp, latitude, longitude, speed, fuel_level, engine_temp, tire_pressure) 
VALUES ('VH002', toTimestamp(now()) - 600000, 40.7591, -73.9849, 50.1, 91.8, 89.3, 32.9);

INSERT INTO vehicle_telemetry (vehicle_id, timestamp, latitude, longitude, speed, fuel_level, engine_temp, tire_pressure) 
VALUES ('VH003', toTimestamp(now()), 40.6892, -74.0445, 0.0, 78.3, 95.2, 30.5);

INSERT INTO vehicle_telemetry (vehicle_id, timestamp, latitude, longitude, speed, fuel_level, engine_temp, tire_pressure) 
VALUES ('VH003', toTimestamp(now()) - 900000, 40.6890, -74.0447, 25.7, 79.1, 94.8, 30.8);

-- Insert vehicle summary data
INSERT INTO vehicle_summary (vehicle_id, last_seen, current_speed, current_fuel_level, current_engine_temp, total_records, last_latitude, last_longitude) 
VALUES ('VH001', toTimestamp(now()), 65.5, 85.2, 90.5, 2, 40.7128, -74.0060);

INSERT INTO vehicle_summary (vehicle_id, last_seen, current_speed, current_fuel_level, current_engine_temp, total_records, last_latitude, last_longitude) 
VALUES ('VH002', toTimestamp(now()), 45.3, 92.5, 88.7, 2, 40.7589, -73.9851);

INSERT INTO vehicle_summary (vehicle_id, last_seen, current_speed, current_fuel_level, current_engine_temp, total_records, last_latitude, last_longitude) 
VALUES ('VH003', toTimestamp(now()), 0.0, 78.3, 95.2, 2, 40.6892, -74.0445);

-- Insert sample alerts
INSERT INTO vehicle_alerts (vehicle_id, alert_id, timestamp, alert_type, severity, message, resolved) 
VALUES ('VH003', uuid(), toTimestamp(now()), 'HIGH_TEMPERATURE', 'WARNING', 'Engine temperature is above normal range', false);

INSERT INTO vehicle_alerts (vehicle_id, alert_id, timestamp, alert_type, severity, message, resolved) 
VALUES ('VH001', uuid(), toTimestamp(now()) - 1800000, 'LOW_FUEL', 'INFO', 'Fuel level is below 20%', true);


