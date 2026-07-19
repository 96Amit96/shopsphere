package com.shopsphere.authservice.entity;

import com.shopsphere.authservice.entity.base.BaseEntity;
import com.shopsphere.authservice.enums.RoleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType name;

    @Column(length = 255)
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<AuthUser> users = new HashSet<>();

    // Helper methods
    public void addUser(AuthUser user) {
        this.users.add(user);
        user.getRoles().add(this);
    }

    public void removeUser(AuthUser user) {
        this.users.remove(user);
        user.getRoles().remove(this);
    }

}
