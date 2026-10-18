package com.shopsphere.paymentservice.config;

import com.shopsphere.paymentservice.dto.event.PaymentRequestedEvent;
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
public class PaymentRequestKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, PaymentRequestedEvent>
    paymentRequestedConsumerFactory(
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
                PaymentRequestedEvent.class.getName()
        );

        props.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "com.shopsphere.paymentservice.dto.event"
        );

        props.put(
                JsonDeserializer.USE_TYPE_INFO_HEADERS,
                false
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "payment-request-consumer"
        );

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRequestedEvent>
    paymentRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentRequestedEvent> consumerFactory
    ) {
        var factory =
                new ConcurrentKafkaListenerContainerFactory
                        <String, PaymentRequestedEvent>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}
