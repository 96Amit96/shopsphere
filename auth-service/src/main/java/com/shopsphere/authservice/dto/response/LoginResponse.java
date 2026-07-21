package com.shopsphere.authservice.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}
