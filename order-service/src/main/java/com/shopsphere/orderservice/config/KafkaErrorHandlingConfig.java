package com.shopsphere.orderservice.config;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.shopsphere.orderservice.dto.event.PaymentEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class KafkaErrorHandlingConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ---------------------------------------------------------
    // PaymentEvent KafkaTemplate
    // ---------------------------------------------------------

    @Bean
    public KafkaTemplate<String, PaymentEvent>
    paymentEventKafkaTemplate() {

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
    // Raw byte[] KafkaTemplate
    // ---------------------------------------------------------

    @Bean
    public KafkaTemplate<String, byte[]>
    byteArrayKafkaTemplate() {

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
            KafkaTemplate<String, PaymentEvent> paymentEventKafkaTemplate,
            KafkaTemplate<String, byte[]> byteArrayKafkaTemplate
    ) {

        Map<Class<?>, KafkaOperations<?, ?>> templates =
                new LinkedHashMap<>();

        templates.put(
                PaymentEvent.class,
                paymentEventKafkaTemplate
        );

        templates.put(
                byte[].class,
                byteArrayKafkaTemplate
        );

        return new DeadLetterPublishingRecoverer(
                templates
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
                new FixedBackOff(
                        2000L,
                        2L
                );

        return new DefaultErrorHandler(
                recoverer,
                backOff
        );
    }

}
