package com.shopsphere.authservice.service;

import com.shopsphere.authservice.dto.request.LoginRequest;
import com.shopsphere.authservice.dto.request.RegisterRequest;
import com.shopsphere.authservice.dto.response.LoginResponse;
import com.shopsphere.authservice.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
