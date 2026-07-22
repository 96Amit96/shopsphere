package com.shopsphere.authservice.dto.response;

import java.util.Set;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        Set<String> roles
) {
}
