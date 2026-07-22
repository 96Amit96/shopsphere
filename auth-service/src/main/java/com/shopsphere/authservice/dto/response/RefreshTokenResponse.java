package com.shopsphere.authservice.dto.response;

public record RefreshTokenResponse(
        String accessToken,
        String tokenType,
        String refreshToken
) {
}
