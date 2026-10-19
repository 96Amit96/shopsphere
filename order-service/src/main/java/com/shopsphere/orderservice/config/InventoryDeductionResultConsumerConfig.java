package com.shopsphere.orderservice.config;

import com.shopsphere.orderservice.dto.event.InventoryDeductionResultEvent;
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
public class InventoryDeductionResultConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
                String, InventoryDeductionResultEvent>
    inventoryDeductionResultKafkaListenerContainerFactory(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> props =
                new HashMap<>(kafkaProperties.buildConsumerProperties());

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-inventory-deduction-results"
        );
        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );
        props.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class
        );
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class
        );

        props.put(
                ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                StringDeserializer.class
        );
        props.put(
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class
        );
        props.put(
                JsonDeserializer.VALUE_DEFAULT_TYPE,
                InventoryDeductionResultEvent.class.getName()
        );
        props.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "com.shopsphere.orderservice.dto.event"
        );
        props.put(
                JsonDeserializer.USE_TYPE_INFO_HEADERS,
                false
        );

        ConsumerFactory<String, InventoryDeductionResultEvent>
                consumerFactory =
                new DefaultKafkaConsumerFactory<>(props);

        ConcurrentKafkaListenerContainerFactory<
                String, InventoryDeductionResultEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.RECORD);

        return factory;
    }
}
