package com.shopsphere.cartservice.client;

import com.shopsphere.cartservice.config.FeignConfig;
import com.shopsphere.cartservice.dto.response.ApiResponse;
import com.shopsphere.cartservice.dto.response.CurrentUserResponse;
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
