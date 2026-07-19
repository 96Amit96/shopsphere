package com.shopsphere.authservice.controller;

import com.shopsphere.authservice.dto.request.RegisterRequest;
import com.shopsphere.authservice.dto.response.ApiResponse;
import com.shopsphere.authservice.dto.response.RegisterResponse;
import com.shopsphere.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = authService.register(request);

        ApiResponse<RegisterResponse> apiResponse = new ApiResponse<>(
                true,
                "User registered successfully",
                response
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }
}
