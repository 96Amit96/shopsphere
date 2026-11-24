package com.shopsphere.orderservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import org.apache.kafka.common.TopicPartition;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class KafkaErrorHandlingConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ---------------------------------------------------------
    // JSON KafkaTemplate for all event payload types
    // ---------------------------------------------------------

    @Bean
    public KafkaTemplate<String, Object> jsonKafkaTemplate() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );

        return new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(props)
        );
    }

    // ---------------------------------------------------------
    // Raw byte[] KafkaTemplate for deserialization failures
    // ---------------------------------------------------------

    @Bean
    public KafkaTemplate<String, byte[]> byteArrayKafkaTemplate() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                ByteArraySerializer.class
        );

        return new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(props)
        );
    }

    // ---------------------------------------------------------
    // Dead Letter Publisher
    // ---------------------------------------------------------

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            @Qualifier("jsonKafkaTemplate")
            KafkaTemplate<String, Object> jsonKafkaTemplate,

            @Qualifier("byteArrayKafkaTemplate")
            KafkaTemplate<String, byte[]> byteArrayKafkaTemplate
    ) {

        Map<Class<?>, KafkaOperations<?, ?>> templates =
                new LinkedHashMap<>();

        // Keep byte[] first: a deserialization failure can produce
        // a null value with the original bytes stored in a header.
        templates.put(
                byte[].class,
                byteArrayKafkaTemplate
        );

        // Fallback for all successfully deserialized event DTOs.
        templates.put(
                Object.class,
                jsonKafkaTemplate
        );

        return new DeadLetterPublishingRecoverer(
                templates,
                (record,exception) -> new TopicPartition(
                        record.topic() + ".DLT",
                        record.partition()
                )
        );
    }

    // ---------------------------------------------------------
    // Retry + DLT
    // ---------------------------------------------------------

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            DeadLetterPublishingRecoverer recoverer
    ) {

        FixedBackOff backOff =
                new FixedBackOff(2000L, 2L);

        return new DefaultErrorHandler(
                recoverer,
                backOff
        );
    }
}
