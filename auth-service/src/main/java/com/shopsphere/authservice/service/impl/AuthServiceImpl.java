package com.shopsphere.authservice.service.impl;

import com.shopsphere.authservice.dto.request.RegisterRequest;
import com.shopsphere.authservice.dto.response.RegisterResponse;
import com.shopsphere.authservice.entity.AuthUser;
import com.shopsphere.authservice.entity.Role;
import com.shopsphere.authservice.enums.RoleType;
import com.shopsphere.authservice.exception.DuplicateResourceException;
import com.shopsphere.authservice.exception.ResourceNotFoundException;
import com.shopsphere.authservice.mapper.AuthUserMapper;
import com.shopsphere.authservice.repository.AuthUserRepository;
import com.shopsphere.authservice.repository.RoleRepository;
import com.shopsphere.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUserMapper authUserMapper;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if(authUserRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists");
        }

        if(authUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        if(authUserRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("Phone number already exists");
        }

        Role role = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Default role ROLE_USER not found"));

        log.info("Role {}",role);

        String encodedPassword = passwordEncoder.encode(request.password());

       AuthUser authUser = new AuthUser();

       authUser.setUsername(request.username());
       authUser.setEmail(request.email());
       authUser.setPhoneNumber(request.phoneNumber());
       authUser.setPassword(encodedPassword);

        authUser.addRole(role);

        AuthUser savedUser = authUserRepository.save(authUser);

        return authUserMapper.toRegisterResponse(savedUser);
    }
}
