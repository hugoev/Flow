package com.vehicletelemetry.processing.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;

/**
 * CassandraConfig - Database Configuration
 *
 * Configures Apache Cassandra connection and keyspace settings.
 * Optimized for high-performance telemetry data storage and retrieval.
 *
 * Clean Architecture: This configuration provides the infrastructure
 * needed for the repository layer to operate.
 */
@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${spring.cassandra.keyspace-name:vehicle_telemetry}")
    private String keyspaceName;

    @Value("${spring.cassandra.contact-points:localhost}")
    private String contactPoints;

    @Value("${spring.cassandra.port:9042}")
    private int port;

    @Value("${spring.cassandra.username:cassandra}")
    private String username;

    @Value("${spring.cassandra.password:cassandra}")
    private String password;

    @Value("${spring.cassandra.schema-action:CREATE_IF_NOT_EXISTS}")
    private SchemaAction schemaAction;

    @Value("${spring.cassandra.keyspace-replication-factor:1}")
    private int replicationFactor;

    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected int getPort() {
        return port;
    }

    protected String getUsername() {
        return username;
    }

    protected String getPassword() {
        return password;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return schemaAction;
    }

    /**
     * Defines keyspace creation specifications.
     * Creates the keyspace with simple replication strategy for development.
     * In production, consider using NetworkTopologyStrategy for multiple
     * datacenters.
     */
    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        // Keyspace already exists, no need to create it
        return Arrays.asList();
    }

    /**
     * Let Spring Data Cassandra automatically create tables based on entity
     * annotations.
     * This ensures consistency between entity mapping and table structure.
     */
    @Override
    protected List<String> getStartupScripts() {
        return Arrays.asList();
    }
}
