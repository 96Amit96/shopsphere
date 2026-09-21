package com.shopsphere.orderservice.security;

import com.shopsphere.orderservice.security.jwt.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InternalServiceTokenProvider {

    private final JwtService jwtService;

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken() {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject("order-service")
                .claim("tokenType", "SERVICE")
                .setIssuedAt(new Date(now))
                .setExpiration(
                        new Date(now + 5 * 60 * 1000)
                )
                .signWith(jwtService.getSigningKey())
                .compact();
    }
}
