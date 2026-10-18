package com.shopsphere.orderservice.config;

import com.shopsphere.orderservice.dto.event.InventoryCompensationItemEvent;
import com.shopsphere.orderservice.dto.event.InventoryCompensationRequestedEvent;
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
public class InventoryCompensationKafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, InventoryCompensationRequestedEvent>
    inventoryCompensationKafkaTemplate(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> props =
                new HashMap<>(kafkaProperties.buildProducerProperties());

        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );

        ProducerFactory<String, InventoryCompensationRequestedEvent>
                producerFactory =
                new DefaultKafkaProducerFactory<>(props);

        return new KafkaTemplate<>(producerFactory);
    }

}
