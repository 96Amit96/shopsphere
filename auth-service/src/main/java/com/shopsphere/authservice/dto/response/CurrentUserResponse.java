package com.shopsphere.authservice.dto.response;

public record CurrentUserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email
) {
}
