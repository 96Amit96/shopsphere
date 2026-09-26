package com.shopsphere.orderservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class Resilience4jLoggingConfig {

    public Resilience4jLoggingConfig(
            CircuitBreakerRegistry registry) {

        registry.getEventPublisher()
                .onEntryAdded(event -> {

                    CircuitBreaker circuitBreaker =
                            event.getAddedEntry();

                    log.info(
                            "CircuitBreaker created :: {}",
                            circuitBreaker.getName()
                    );

                    circuitBreaker.getEventPublisher()
                            .onStateTransition(stateEvent ->
                                    log.warn(
                                            "CircuitBreaker '{}' state changed :: {}",
                                            stateEvent.getCircuitBreakerName(),
                                            stateEvent.getStateTransition()
                                    )
                            );
                });
    }
}
