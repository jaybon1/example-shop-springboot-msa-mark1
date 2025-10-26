package com.example.shop.user.infrastructure.jpa.mapper;

import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.model.UserSocial;
import com.example.shop.user.infrastructure.jpa.entity.UserEntity;
import com.example.shop.user.infrastructure.jpa.entity.UserRoleEntity;
import com.example.shop.user.infrastructure.jpa.entity.UserSocialEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        return User.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .nickname(userEntity.getNickname())
                .email(userEntity.getEmail())
                .jwtValidator(userEntity.getJwtValidator())
                .userRoleList(userEntity.getUserRoleList()
                        .stream()
                        .map(this::toDomain)
                        .toList())
                .userSocialList(userEntity.getUserSocialList()
                        .stream()
                        .map(this::toDomain)
                        .toList())
                .createdAt(userEntity.getCreatedAt())
                .createdBy(userEntity.getCreatedBy())
                .updatedAt(userEntity.getUpdatedAt())
                .updatedBy(userEntity.getUpdatedBy())
                .deletedAt(userEntity.getDeletedAt())
                .deletedBy(userEntity.getDeletedBy())
                .build();
    }

    private UserRole toDomain(UserRoleEntity userRoleEntity) {
        if (userRoleEntity == null) {
            return null;
        }
        return UserRole.builder()
                .id(userRoleEntity.getId())
                .role(toDomain(userRoleEntity.getRole()))
                .createdAt(userRoleEntity.getCreatedAt())
                .createdBy(userRoleEntity.getCreatedBy())
                .updatedAt(userRoleEntity.getUpdatedAt())
                .updatedBy(userRoleEntity.getUpdatedBy())
                .deletedAt(userRoleEntity.getDeletedAt())
                .deletedBy(userRoleEntity.getDeletedBy())
                .build();
    }

    private UserRole.Role toDomain(UserRoleEntity.Role role) {
        if (role == null) {
            return null;
        }
        return UserRole.Role.valueOf(role.name());
    }

    private UserSocial toDomain(UserSocialEntity userSocialEntity) {
        if (userSocialEntity == null) {
            return null;
        }
        return UserSocial.builder()
                .id(userSocialEntity.getId())
                .provider(toDomain(userSocialEntity.getProvider()))
                .providerId(userSocialEntity.getProviderId())
                .nickname(userSocialEntity.getNickname())
                .email(userSocialEntity.getEmail())
                .createdAt(userSocialEntity.getCreatedAt())
                .createdBy(userSocialEntity.getCreatedBy())
                .updatedAt(userSocialEntity.getUpdatedAt())
                .updatedBy(userSocialEntity.getUpdatedBy())
                .deletedAt(userSocialEntity.getDeletedAt())
                .deletedBy(userSocialEntity.getDeletedBy())
                .build();
    }

    private UserSocial.Provider toDomain(UserSocialEntity.Provider provider) {
        if (provider == null) {
            return null;
        }
        return UserSocial.Provider.valueOf(provider.name());
    }

    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }

        UserEntity userEntity = UserEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .jwtValidator(user.getJwtValidator())
                .build();

        user.getUserRoleList()
                .stream()
                .map(this::toEntity)
                .forEach(userEntity::add);

        user.getUserSocialList()
                .stream()
                .map(this::toEntity)
                .forEach(userEntity::add);

        if (user.getDeletedAt() != null) {
            Optional.ofNullable(toUuid(user.getDeletedBy()))
                    .ifPresent(uuid -> userEntity.markDeleted(user.getDeletedAt(), uuid));
        }

        return userEntity;
    }

    private UserRoleEntity toEntity(UserRole userRole) {
        if (userRole == null) {
            return null;
        }
        return UserRoleEntity.builder()
                .id(userRole.getId())
                .role(toEntity(userRole.getRole()))
                .build();
    }

    private UserRoleEntity.Role toEntity(UserRole.Role role) {
        if (role == null) {
            return null;
        }
        return UserRoleEntity.Role.valueOf(role.name());
    }

    private UserSocialEntity toEntity(UserSocial userSocial) {
        if (userSocial == null) {
            return null;
        }
        return UserSocialEntity.builder()
                .id(userSocial.getId())
                .provider(toEntity(userSocial.getProvider()))
                .providerId(userSocial.getProviderId())
                .nickname(userSocial.getNickname())
                .email(userSocial.getEmail())
                .build();
    }

    private UserSocialEntity.Provider toEntity(UserSocial.Provider provider) {
        if (provider == null) {
            return null;
        }
        return UserSocialEntity.Provider.valueOf(provider.name());
    }

    public void applyDomain(User user, UserEntity userEntity) {
        if (user == null || userEntity == null) {
            return;
        }

        userEntity.update(user.getNickname(), user.getEmail(), user.getJwtValidator());
        userEntity.updatePassword(user.getPassword());

        syncUserRoleList(user, userEntity);
        syncUserSocialList(user, userEntity);

        if (user.getDeletedAt() != null) {
            Optional.ofNullable(toUuid(user.getDeletedBy()))
                    .ifPresent(uuid -> userEntity.markDeleted(user.getDeletedAt(), uuid));
        }
    }

    private void syncUserRoleList(User user, UserEntity userEntity) {
        Map<UUID, UserRoleEntity> currentRoleMap = userEntity.getUserRoleList()
                .stream()
                .filter(role -> role.getId() != null)
                .collect(Collectors.toMap(UserRoleEntity::getId, Function.identity()));

        Set<UUID> desiredRoleIds = new HashSet<>();
        user.getUserRoleList().forEach(userRole -> {
            UUID roleId = userRole.getId();
            if (roleId != null && currentRoleMap.containsKey(roleId)) {
                UserRoleEntity existingRole = currentRoleMap.get(roleId);
                existingRole.updateRole(toEntity(userRole.getRole()));
                desiredRoleIds.add(roleId);
            } else {
                UserRoleEntity userRoleEntity = toEntity(userRole);
                userEntity.add(userRoleEntity);
                if (userRoleEntity.getId() != null) {
                    desiredRoleIds.add(userRoleEntity.getId());
                }
            }
        });

        List<UserRoleEntity> rolesToRemove = userEntity.getUserRoleList()
                .stream()
                .filter(role -> role.getId() != null && !desiredRoleIds.contains(role.getId()))
                .toList();
        rolesToRemove.forEach(userEntity::remove);
    }

    private void syncUserSocialList(User user, UserEntity userEntity) {
        Map<UUID, UserSocialEntity> currentSocialMap = userEntity.getUserSocialList()
                .stream()
                .filter(social -> social.getId() != null)
                .collect(Collectors.toMap(UserSocialEntity::getId, Function.identity()));

        Set<UUID> desiredSocialIds = new HashSet<>();
        user.getUserSocialList().forEach(userSocial -> {
            UUID socialId = userSocial.getId();
            if (socialId != null && currentSocialMap.containsKey(socialId)) {
                UserSocialEntity existingSocial = currentSocialMap.get(socialId);
                existingSocial.update(
                        toEntity(userSocial.getProvider()),
                        userSocial.getProviderId(),
                        userSocial.getNickname(),
                        userSocial.getEmail()
                );
                desiredSocialIds.add(socialId);
            } else {
                UserSocialEntity userSocialEntity = toEntity(userSocial);
                userEntity.add(userSocialEntity);
                if (userSocialEntity.getId() != null) {
                    desiredSocialIds.add(userSocialEntity.getId());
                }
            }
        });

        List<UserSocialEntity> socialToRemove = userEntity.getUserSocialList()
                .stream()
                .filter(social -> social.getId() != null && !desiredSocialIds.contains(social.getId()))
                .toList();
        socialToRemove.forEach(userEntity::remove);
    }

    private UUID toUuid(String value) {
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
