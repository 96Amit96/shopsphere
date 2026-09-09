package com.shopsphere.authservice.service.impl;

import com.shopsphere.authservice.dto.request.LoginRequest;
import com.shopsphere.authservice.dto.request.RefreshTokenRequest;
import com.shopsphere.authservice.dto.request.RegisterRequest;
import com.shopsphere.authservice.dto.response.*;
import com.shopsphere.authservice.entity.AuthUser;
import com.shopsphere.authservice.entity.RefreshToken;
import com.shopsphere.authservice.entity.Role;
import com.shopsphere.authservice.enums.RoleType;
import com.shopsphere.authservice.exception.DuplicateResourceException;
import com.shopsphere.authservice.exception.InvalidCredentialsException;
import com.shopsphere.authservice.exception.ResourceNotFoundException;
import com.shopsphere.authservice.mapper.AuthUserMapper;
import com.shopsphere.authservice.repository.AuthUserRepository;
import com.shopsphere.authservice.repository.RoleRepository;
import com.shopsphere.authservice.security.jwt.JwtService;
import com.shopsphere.authservice.security.jwt.RefreshTokenService;
import com.shopsphere.authservice.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUserMapper authUserMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;


    @Override
    public RegisterResponse register(RegisterRequest request) {
        log.info("request: {}", request);

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
       authUser.setFirstName(request.firstName());
       authUser.setLastName(request.lastName());
       authUser.setEmail(request.email());
       authUser.setPhoneNumber(request.phoneNumber());
       authUser.setPassword(encodedPassword);

        authUser.addRole(role);

        AuthUser savedUser = authUserRepository.save(authUser);

        return authUserMapper.toRegisterResponse(savedUser);
    }



    @Override
    public LoginResponse login(LoginRequest request) {

        log.info("Login Request: {} ", request);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }


        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(authUser);
        log.info("accessToken Token {}", accessToken);
        String refreshToken = refreshTokenService.createRefreshToken(authUser);
        log.info("refreshToken Token {}", refreshToken);

        Set<String> roles = authUser.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        log.info("roles: {}", roles);

        UserSummaryResponse user = new UserSummaryResponse(
                authUser.getId(),
                authUser.getUsername(),
                authUser.getFirstName(),
                authUser.getLastName(),
                authUser.getEmail(),
                roles
        );

        return new LoginResponse(
                user,
                accessToken,
                "Bearer",
                refreshToken
        );
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(
                request.refreshToken()
        );

        AuthUser authUser =oldRefreshToken.getAuthUser();

        refreshTokenService.revokeRefreshToken(
                request.refreshToken()
        );

        String newAccessToken = jwtService.generateToken(authUser);

        String newRefreshToken = refreshTokenService.createRefreshToken(authUser);

        return new RefreshTokenResponse(
                newAccessToken,
                "Bearer",
                newRefreshToken
        );
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {

        refreshTokenService.revokeRefreshToken(rawRefreshToken);
        log.info("User logged out successfully");
    }

    @Override
    public CurrentUserResponse getCurrentUser(String email) {

        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email :: " + email
                        )
                );

        log.info("User:: {}", user);

        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
    }
}
