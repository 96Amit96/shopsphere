package com.shopsphere.paymentservice.dto.response;

public record CurrentUserResponse(

        Long id,
        String username,
        String firstName,
        String lastName,
        String email
) {
}
