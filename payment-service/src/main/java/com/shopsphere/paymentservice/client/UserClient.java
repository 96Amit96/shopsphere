package com.shopsphere.paymentservice.client;

import com.shopsphere.paymentservice.config.FeignConfig;
import com.shopsphere.paymentservice.dto.response.ApiResponse;
import com.shopsphere.paymentservice.dto.response.CurrentUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "auth-service",
        configuration = FeignConfig.class
)
public interface UserClient {

    @GetMapping("/api/v1/users/me")
    ApiResponse<CurrentUserResponse> getCurrentUser();
}
