package com.shopsphere.authservice.controller;

import com.shopsphere.authservice.dto.response.ApiResponse;
import com.shopsphere.authservice.dto.response.CurrentUserResponse;
import com.shopsphere.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/users")
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(Authentication authentication) {

        CurrentUserResponse response =
                authService.getCurrentUser(authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current user fetched successfully",
                        response
                )
        );
    }
}
