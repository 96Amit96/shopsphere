package com.shopsphere.authservice.security.jwt;

import com.shopsphere.authservice.config.JwtProperties;
import com.shopsphere.authservice.entity.AuthUser;
import com.shopsphere.authservice.entity.RefreshToken;
import com.shopsphere.authservice.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    private final SecureRandom secureRandom = new SecureRandom();


    /**
     * Creates and persists a new refresh token.
     *
     * @param authUser authenticated user
     * @return raw refresh token that will be sent to the client
     */
    @Transactional
    public String createRefreshToken(AuthUser authUser) {

        // Generate cryptographically secure random bytes
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);

        // Convert random bytes to URL-safe token
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Hash token before storing it in DB
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setTokenHash(tokenHash);

        refreshToken.setExpiryDate(
                LocalDateTime.now()
                        .plusNanos(
                                jwtProperties.getRefreshExpiration()
                                        * 1_000_000
                        )
        );

        refreshToken.setRevoked(false);
        refreshToken.setAuthUser(authUser);

        refreshTokenRepository.save(refreshToken);

        // Return raw token to client
        // Raw token is NOT stored in database
        return rawToken;
    }


    /**
     * Validates a refresh token.
     *
     * @param rawToken refresh token received from client
     * @return valid RefreshToken entity
     */
    @Transactional
    public RefreshToken validateRefreshToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid refresh token"
                        )
                );

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }


    /**
     * Revokes a refresh token.
     *
     * @param rawToken refresh token received from client
     */
    @Transactional
    public void revokeRefreshToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid refresh token"
                                )
                        );

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }


    /**
     * Creates SHA-256 hash of refresh token.
     */
    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    e
            );
        }
    }
}