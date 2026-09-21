package com.shopsphere.orderservice.config;

import com.shopsphere.orderservice.security.InternalServiceTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@RequiredArgsConstructor
public class InventoryFeignConfig {

    private final InternalServiceTokenProvider tokenProvider;

    @Bean
    public RequestInterceptor inventoryRequestInterceptor() {

        return requestTemplate -> {

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            // Normal HTTP request
            if (attributes != null) {

                HttpServletRequest request =
                        attributes.getRequest();

                String authorization =
                        request.getHeader("Authorization");

                if (authorization != null &&
                        !authorization.isBlank()) {

                    requestTemplate.header(
                            "Authorization",
                            authorization
                    );

                    return;
                }
            }

            // Background call, e.g. Kafka consumer
            String serviceToken =
                    tokenProvider.generateToken();

            requestTemplate.header(
                    "Authorization",
                    "Bearer " + serviceToken
            );
        };
    }
}
