package com.example.shop.user.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class User {

    private final UUID id;
    private final String username;
    private final String password;
    private final String nickname;
    private final String email;
    private final Long jwtValidator;
    private final List<UserRole> userRoleList;
    private final List<UserSocial> userSocialList;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant updatedAt;
    private final String updatedBy;
    private final Instant deletedAt;
    private final String deletedBy;

    public boolean hasRole(UserRole.Role role) {
        if (role == null || userRoleList == null) {
            return false;
        }
        return userRoleList.stream()
                .map(UserRole::getRole)
                .anyMatch(role::equals);
    }

    public User markDeleted(Instant deletedAt, UUID userId) {
        return this.toBuilder()
                .deletedAt(deletedAt)
                .deletedBy(userId != null ? userId.toString() : null)
                .build();
    }

    public List<UserRole> getUserRoleList() {
        return userRoleList == null ? List.of() : Collections.unmodifiableList(userRoleList);
    }

    public List<UserSocial> getUserSocialList() {
        return userSocialList == null ? List.of() : Collections.unmodifiableList(userSocialList);
    }

    public User addRole(UserRole userRole) {
        if (userRole == null) {
            return this;
        }
        List<UserRole> updatedRoleList = new ArrayList<>(getUserRoleList());
        updatedRoleList.add(userRole);
        return this.toBuilder()
                .userRoleList(List.copyOf(updatedRoleList))
                .build();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    UserBuilder toBuilder() {
        return User.builder()
                .id(id)
                .username(username)
                .password(password)
                .nickname(nickname)
                .email(email)
                .jwtValidator(jwtValidator)
                .userRoleList(userRoleList)
                .userSocialList(userSocialList)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .deletedAt(deletedAt)
                .deletedBy(deletedBy);
    }
}
