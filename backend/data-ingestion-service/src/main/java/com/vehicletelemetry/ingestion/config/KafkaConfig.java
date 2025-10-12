package com.vehicletelemetry.ingestion.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.vehicletelemetry.ingestion.model.TelemetryData;

/**
 * KafkaConfig - Infrastructure Configuration
 *
 * This configuration class sets up Kafka producer for publishing telemetry
 * data.
 * It follows the Dependency Inversion Principle by providing abstractions
 * that the service layer can use without knowing implementation details.
 *
 * Clean Architecture: This is part of the infrastructure layer that
 * handles external dependencies like Kafka.
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.acks:all}")
    private String acks;

    @Value("${spring.kafka.producer.retries:3}")
    private int retries;

    @Value("${spring.kafka.producer.batch-size:16384}")
    private int batchSize;

    @Value("${spring.kafka.producer.linger-ms:1}")
    private int lingerMs;

    @Value("${spring.kafka.producer.buffer-memory:33554432}")
    private int bufferMemory;

    /**
     * Creates Kafka producer configuration properties.
     * These settings are optimized for reliable, high-throughput telemetry data
     * publishing.
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();

        // Basic connection settings
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Serialization settings
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);

        // Performance settings
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);

        // Idempotence settings for exactly-once delivery
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return props;
    }

    /**
     * Creates the Kafka producer factory with telemetry data serialization.
     */
    @Bean
    public ProducerFactory<String, TelemetryData> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * Creates the Kafka template for publishing telemetry data.
     * This is the main interface that services will use to publish messages.
     */
    @Bean
    public KafkaTemplate<String, TelemetryData> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Creates a String-based Kafka template for MQTT bridge service.
     * This is needed for the MqttKafkaBridgeService which works with raw JSON
     * strings.
     */
    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        Map<String, Object> stringProps = new HashMap<>(producerConfigs());
        stringProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        ProducerFactory<String, String> stringFactory = new DefaultKafkaProducerFactory<>(stringProps);
        return new KafkaTemplate<>(stringFactory);
    }

}
