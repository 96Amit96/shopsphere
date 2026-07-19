package com.shopsphere.authservice.repository;

import com.shopsphere.authservice.entity.Role;
import com.shopsphere.authservice.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleType name);

    boolean existsByName(RoleType name);

}
