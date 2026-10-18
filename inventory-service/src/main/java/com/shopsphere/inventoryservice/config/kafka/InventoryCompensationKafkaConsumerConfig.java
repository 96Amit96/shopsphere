package com.shopsphere.inventoryservice.config.kafka;

import com.shopsphere.inventoryservice.dto.event.InventoryCompensationRequestedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class InventoryCompensationKafkaConsumerConfig {

    @Bean(name = "inventoryCompensationRequestKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<
                String, InventoryCompensationRequestedEvent>
    inventoryCompensationRequestKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> properties =
                new HashMap<>(kafkaProperties.buildConsumerProperties());

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "inventory-compensation-consumer"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class
        );

        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class
        );

        properties.put(
                ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                StringDeserializer.class
        );

        properties.put(
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class
        );

        properties.put(
                JsonDeserializer.VALUE_DEFAULT_TYPE,
                InventoryCompensationRequestedEvent.class.getName()
        );

        properties.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "com.shopsphere.inventoryservice.dto.event"
        );

        properties.put(
                JsonDeserializer.USE_TYPE_INFO_HEADERS,
                false
        );

        ConsumerFactory<String, InventoryCompensationRequestedEvent>
                consumerFactory =
                new DefaultKafkaConsumerFactory<>(properties);

        ConcurrentKafkaListenerContainerFactory<
                String, InventoryCompensationRequestedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.RECORD);

        return factory;
    }

}
