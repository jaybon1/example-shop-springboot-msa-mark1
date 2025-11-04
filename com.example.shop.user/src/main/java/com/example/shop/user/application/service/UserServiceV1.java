package com.example.shop.user.application.service;

import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.repository.UserRepository;
import com.example.shop.user.infrastructure.redis.client.AuthRedisClient;
import com.example.shop.user.presentation.advice.UserError;
import com.example.shop.user.presentation.advice.UserException;
import com.example.shop.user.presentation.dto.response.ResGetUsersDtoV1;
import com.example.shop.user.presentation.dto.response.ResGetUserDtoV1;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceV1 {

    private final UserRepository userRepository;

    private final AuthRedisClient authRedisClient;

    public ResGetUsersDtoV1 getUsers(
            UUID authUserId,
            List<String> authUserRoleList,
            Pageable pageable,
            String username,
            String nickname,
            String email
    ) {
        if (!isAdmin(authUserRoleList) && !isManager(authUserRoleList)) {
            throw new UserException(UserError.USER_FORBIDDEN);
        }

        String normalizedUsername = normalize(username);
        String normalizedNickname = normalize(nickname);
        String normalizedEmail = normalize(email);

        Page<User> userPage = userRepository.searchUsers(normalizedUsername, normalizedNickname, normalizedEmail, pageable);

        return ResGetUsersDtoV1.of(userPage);
    }

    public ResGetUserDtoV1 getUser(UUID authUserId, List<String> authUserRoleList, UUID userId) {
        User user = getUserOrThrow(userId);
        validateAccess(authUserId, authUserRoleList, user);
        return ResGetUserDtoV1.of(user);
    }

    @Transactional
    public void deleteUser(UUID authUserId, List<String> authUserRoleList, UUID userId) {
        User user = getUserOrThrow(userId);
        validateAccess(authUserId, authUserRoleList, user);
        boolean targetIsAdmin = user.getUserRoleList().stream()
                .map(UserRole::getRole)
                .anyMatch(role -> role == UserRole.Role.ADMIN);
        if (targetIsAdmin) {
            throw new UserException(UserError.USER_BAD_REQUEST);
        }
        User deletedUser = user.markDeleted(Instant.now(), authUserId);
        authRedisClient.denyBy(String.valueOf(deletedUser.getId()), Instant.now().getEpochSecond());
        userRepository.save(deletedUser);
    }

    private void validateAccess(UUID authUserId, List<String> authUserRoleList, User targetUser) {
        boolean targetIsAdmin = targetUser.getUserRoleList().stream()
                .map(UserRole::getRole)
                .anyMatch(role -> role == UserRole.Role.ADMIN);
        if (targetIsAdmin && !isAdmin(authUserRoleList)) {
            throw new UserException(UserError.USER_BAD_REQUEST);
        }

        if ((authUserId != null && authUserId.equals(targetUser.getId()))
                || isAdmin(authUserRoleList)
                || isManager(authUserRoleList)) {
            return;
        }
        throw new UserException(UserError.USER_BAD_REQUEST);
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserError.USER_CAN_NOT_FOUND));
    }

    private boolean isAdmin(List<String> authUserRoleList) {
        return !CollectionUtils.isEmpty(authUserRoleList)
                && authUserRoleList.contains(UserRole.Role.ADMIN.toString());
    }

    private boolean isManager(List<String> authUserRoleList) {
        return !CollectionUtils.isEmpty(authUserRoleList)
                && authUserRoleList.contains(UserRole.Role.MANAGER.toString());
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
