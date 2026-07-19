package com.shopsphere.authservice.initializer;

import com.shopsphere.authservice.entity.Role;
import com.shopsphere.authservice.enums.RoleType;
import com.shopsphere.authservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRoleIfNotExists(
                RoleType.ROLE_USER,
                "Default customer role");

        createRoleIfNotExists(
                RoleType.ROLE_ADMIN,
                "Administrator role");
    }

    private void createRoleIfNotExists(
            RoleType roleType,
            String description) {

        if (roleRepository.existsByName(roleType)) {
            log.info("{} already exists", roleType);
            return;
        }

        Role role = new Role();
        role.setName(roleType);
        role.setDescription(description);

        log.info("Creating role {}", roleType);
        roleRepository.save(role);
    }
}

