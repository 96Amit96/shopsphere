package com.shopsphere.authservice.service;

import com.shopsphere.authservice.dto.request.RegisterRequest;
import com.shopsphere.authservice.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
}
