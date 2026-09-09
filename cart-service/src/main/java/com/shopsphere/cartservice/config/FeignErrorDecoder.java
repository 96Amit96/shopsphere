package com.shopsphere.cartservice.config;

import com.shopsphere.cartservice.exception.*;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        int status = response.status();

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
