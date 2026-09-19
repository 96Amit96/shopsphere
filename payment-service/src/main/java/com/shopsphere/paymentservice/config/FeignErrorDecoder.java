package com.shopsphere.paymentservice.config;

import com.shopsphere.paymentservice.exception.*;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        log.error(
                "FeignErrorDecoder invoked :: methodKey={}, status={}",
                methodKey,
                response.status()
        );

        int status = response.status();

        if (status == 409 && methodKey.startsWith("InventoryClient#reserveStock")) {

            log.error(
                    "Inventory reservation failed :: methodKey={}, status={}",
                    methodKey,
                    status
            );

            return new InventoryReservationException(
                    "Insufficient inventory while creating order"
            );
        }

        if (status == 401) {
            return new UnauthorizedException(
                    "Unauthorized request to downstream service"
            );
        }

        if (status == 403) {
            return new ForbiddenException(
                    "Access denied by downstream service"
            );
        }

        if (status == 404) {

            if (methodKey.contains("UserClient")) {
                return new UserNotFoundException(
                        "Authenticated user not found"
                );
            }

            if (methodKey.contains("ProductClient")) {
                return new ProductNotFoundException(
                        "Product not found or inactive"
                );
            }

            return new RuntimeException(
                    "Requested resource not found"
            );
        }

        if (status >= 500) {
            return new ServiceUnavailableException(
                    "Downstream service is currently unavailable"
            );
        }

        return new ErrorDecoder.Default()
                .decode(methodKey, response);
    }
}
