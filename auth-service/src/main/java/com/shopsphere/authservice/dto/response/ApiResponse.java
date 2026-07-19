package com.shopsphere.authservice.dto.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
}
