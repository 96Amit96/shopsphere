package com.shopsphere.inventoryservice.config.kafka;

import com.shopsphere.inventoryservice.dto.event.InventoryCompensationResultEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
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
public class InventoryCompensationResultKafkaConfig {

    @Bean
    public KafkaTemplate<String, InventoryCompensationResultEvent>
    inventoryCompensationResultKafkaTemplate(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> properties =
                new HashMap<>(kafkaProperties.buildProducerProperties());

        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );

        ProducerFactory<String, InventoryCompensationResultEvent> factory =
                new DefaultKafkaProducerFactory<>(properties);

        return new KafkaTemplate<>(factory);
    }
}
