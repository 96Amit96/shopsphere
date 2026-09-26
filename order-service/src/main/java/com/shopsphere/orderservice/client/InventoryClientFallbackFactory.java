package com.shopsphere.orderservice.client;

import com.shopsphere.orderservice.dto.request.StockReservationRequest;
import com.shopsphere.orderservice.dto.response.ApiResponse;
import com.shopsphere.orderservice.dto.response.InventoryResponse;
import com.shopsphere.orderservice.exception.InventoryCircuitOpenException;
import com.shopsphere.orderservice.exception.InventoryServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {

        log.info(
                "Inventory fallback triggered. Cause type :: {}, message :: {}",
                cause.getClass().getName(),
                cause.getMessage()
        );

        return new InventoryClient() {

            @Override
            public ResponseEntity<ApiResponse<InventoryResponse>> reserveStock(
                    Long productId,
                    StockReservationRequest request) {

                if (cause instanceof CallNotPermittedException) {

                    throw new InventoryCircuitOpenException(
                            "Inventory circuit breaker is OPEN"
                    );
                }

                throw new InventoryServiceUnavailableException(
                        "Inventory service is currently unavailable"
                );
            }


            @Override
            public void releaseStock(
                    Long productId,
                    StockReservationRequest request) {

                if (cause instanceof CallNotPermittedException) {

                    throw new InventoryCircuitOpenException(
                            "Inventory circuit breaker is OPEN"
                    );
                }

                throw new InventoryServiceUnavailableException(
                        "Inventory service is currently unavailable"
                );
            }
        };
    }
}
