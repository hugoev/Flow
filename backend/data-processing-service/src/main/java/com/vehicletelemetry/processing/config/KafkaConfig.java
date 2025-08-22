package com.vehicletelemetry.processing.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehicletelemetry.processing.model.TelemetryData;

/**
 * KafkaConfig - Consumer Configuration
 *
 * Configures Kafka consumer for processing telemetry data.
 * Optimized for high-throughput, reliable message processing.
 *
 * Clean Architecture: This configuration provides the infrastructure
 * needed for the consumer layer to operate.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:telemetry-processing-group}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${spring.kafka.consumer.enable-auto-commit:false}")
    private boolean enableAutoCommit;

    @Value("${spring.kafka.consumer.session-timeout:30000}")
    private int sessionTimeout;

    @Value("${spring.kafka.consumer.request-timeout:40000}")
    private int requestTimeout;

    @Value("${spring.kafka.consumer.max-poll-records:500}")
    private int maxPollRecords;

    @Value("${spring.kafka.consumer.fetch-min-size:1024}")
    private int fetchMinSize;

    @Value("${spring.kafka.consumer.fetch-max-wait:500}")
    private int fetchMaxWait;

    /**
     * Creates Kafka consumer configuration properties.
     * These settings are optimized for reliable, high-throughput telemetry data
     * processing.
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();

        // Basic connection settings
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Serialization settings - Use ErrorHandlingDeserializer to handle type issues
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.class);
        props.put(org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                StringDeserializer.class);
        props.put(org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TelemetryData.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        // Offset management settings
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);

        // Timeout settings
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);

        // Performance tuning settings
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinSize);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWait);

        // Reliability settings
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);

        return props;
    }

    /**
     * Creates the Kafka consumer factory.
     */
    @Bean
    public ConsumerFactory<String, TelemetryData> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * Creates the Kafka listener container factory.
     * Configured for manual acknowledgment and batch processing.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TelemetryData> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TelemetryData> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // Enable manual acknowledgment for reliable processing
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Configure concurrency based on the number of partitions
        factory.setConcurrency(3);

        return factory;
    }

    /**
     * Custom ObjectMapper for JSON deserialization.
     * Configured to handle Java 8 time types properly.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Registers Java 8 time modules
        return mapper;
    }
}
