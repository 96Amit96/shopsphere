package com.shopsphere.orderservice.config;

import com.shopsphere.orderservice.dto.event.PaymentRequestedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OrderPaymentRequestKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, PaymentRequestedEvent>
    paymentRequestedProducerFactory(KafkaProperties kafkaProperties) {

        Map<String, Object> config =
                new HashMap<>(
                        kafkaProperties.buildProducerProperties()
                );

        config.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        config.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, PaymentRequestedEvent>
    paymentRequestedKafkaTemplate(
            @Qualifier("paymentRequestedProducerFactory")
            ProducerFactory<String, PaymentRequestedEvent> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
