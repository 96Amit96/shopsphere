package com.shopsphere.authservice.repository;

import com.shopsphere.authservice.entity.AuthUser;
import com.shopsphere.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByAuthUserIdAndRevokedFalse(Long authUserId);

    void deleteByAuthUserId(Long authUserId);
}
