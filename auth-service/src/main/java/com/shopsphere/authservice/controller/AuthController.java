package com.shopsphere.authservice.controller;

import com.shopsphere.authservice.dto.request.LoginRequest;
import com.shopsphere.authservice.dto.request.RegisterRequest;
import com.shopsphere.authservice.dto.response.ApiResponse;
import com.shopsphere.authservice.dto.response.LoginResponse;
import com.shopsphere.authservice.dto.response.RegisterResponse;
import com.shopsphere.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(
                true,
                "Login successful",
                response
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(apiResponse);
    }
}
