package com.shopsphere.orderservice.config;

import com.shopsphere.orderservice.dto.event.InventorySagaEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OrderInventoryEventKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, InventorySagaEvent>
    orderInventoryEventConsumerFactory(
            KafkaProperties kafkaProperties
    ) {

        Map<String, Object> props =
                new HashMap<>(
                        kafkaProperties.buildConsumerProperties()
                );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class
        );

        props.put(
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class
        );

        props.put(
                JsonDeserializer.VALUE_DEFAULT_TYPE,
                InventorySagaEvent.class.getName()
        );

        props.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "com.shopsphere.orderservice.dto.event"
        );

        props.put(
                JsonDeserializer.USE_TYPE_INFO_HEADERS,
                false
        );

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventorySagaEvent>
    orderInventoryEventKafkaListenerContainerFactory(
            ConsumerFactory<String, InventorySagaEvent> consumerFactory
    ) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory
                        <String, InventorySagaEvent>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}
