package com.shopsphere.authservice.entity;

import com.shopsphere.authservice.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "auth_users")
@Getter
@Setter
@NoArgsConstructor
public class AuthUser extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, unique = true, length = 15)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean accountNonExpired = true;

    @Column(nullable = false)
    private Boolean accountNonLocked = true;

    @Column(nullable = false)
    private Boolean credentialsNonExpired = true;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "auth_user_roles",
            joinColumns = @JoinColumn(name = "auth_user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(
            mappedBy = "authUser",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    // Helper Methods
    public void addRole(Role role) {
        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }

        if (!role.getUsers().contains(this)) {
            role.getUsers().add(this);
        }
    }

    public void addRefreshToken(RefreshToken token) {
        refreshTokens.add(token);
        token.setAuthUser(this);
    }

    public void removeRefreshToken(RefreshToken token) {
        refreshTokens.remove(token);
        token.setAuthUser(null);
    }

}
