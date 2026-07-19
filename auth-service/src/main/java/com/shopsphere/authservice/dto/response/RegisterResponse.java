package com.shopsphere.authservice.dto.response;

public record RegisterResponse(
        Long id,
        String username,
        String email,
        String phoneNumber

) {
}
