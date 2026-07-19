package com.shopsphere.authservice.repository;

import com.shopsphere.authservice.entity.AuthUser;
import com.shopsphere.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByAuthUser(AuthUser authUser);
}
