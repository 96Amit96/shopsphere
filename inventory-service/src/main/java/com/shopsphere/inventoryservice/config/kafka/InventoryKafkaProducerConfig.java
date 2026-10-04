package com.shopsphere.inventoryservice.config.kafka;

import com.shopsphere.inventoryservice.dto.event.InventorySagaEvent;
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
public class InventoryKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, InventorySagaEvent> inventorySagaEventProducerFactory(
            KafkaProperties kafkaProperties
    ) {

        Map<String, Object> config =
                new HashMap<>(kafkaProperties.buildProducerProperties());

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
    public KafkaTemplate<String, InventorySagaEvent> inventorySagaEventKafkaTemplate(
            ProducerFactory<String, InventorySagaEvent> producerFactory
    ) {

        return new KafkaTemplate<>(producerFactory);
    }
}
