package com.example.shop.user.application.service;

import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.repository.UserRepository;
import com.example.shop.user.presentation.advice.UserError;
import com.example.shop.user.presentation.advice.UserException;
import com.example.shop.user.presentation.dto.response.ResGetUsersDtoV1;
import com.example.shop.user.presentation.dto.response.ResGetUserDtoV1;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    public ResGetUsersDtoV1 getUsers(
            UUID authUserId,
            List<String> authUserRoleList,
            Pageable pageable,
            String username,
            String nickname,
            String email
    ) {
        String normalizedUsername = normalize(username);
        String normalizedNickname = normalize(nickname);
        String normalizedEmail = normalize(email);

        Page<User> userPage;
        if (isAdmin(authUserRoleList) || isManager(authUserRoleList)) {
            userPage = userRepository.searchUsers(normalizedUsername, normalizedNickname, normalizedEmail, pageable);
        } else {
            if (authUserId == null) {
                throw new UserException(UserError.USER_BAD_REQUEST);
            }
            User user = getUserOrThrow(authUserId);
            if (!matchesFilter(user, normalizedUsername, normalizedNickname, normalizedEmail)) {
                userPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            } else if (pageable.isPaged() && pageable.getPageNumber() > 0) {
                userPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            } else {
                userPage = new PageImpl<>(List.of(user), pageable, 1);
            }
        }

        List<ResGetUsersDtoV1.UserDto> userDtoList = userPage.getContent()
                .stream()
                .map(this::toUserDto)
                .toList();

        return ResGetUsersDtoV1.builder()
                .users(userDtoList)
                .build();
    }

    public ResGetUserDtoV1 getUser(UUID authUserId, List<String> authUserRoleList, UUID userId) {
        User user = getUserOrThrow(userId);
        validateAccess(authUserId, authUserRoleList, user);
        return ResGetUserDtoV1.builder()
                .user(toUserDetailsDto(user))
                .build();
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
        userRepository.save(deletedUser);
    }

    private ResGetUsersDtoV1.UserDto toUserDto(User user) {
        return ResGetUsersDtoV1.UserDto.builder()
                .id(user.getId() != null ? user.getId().toString() : null)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();
    }

    private ResGetUserDtoV1.UserDto toUserDetailsDto(User user) {
        return ResGetUserDtoV1.UserDto.builder()
                .id(user.getId() != null ? user.getId().toString() : null)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();
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

    private boolean matchesFilter(User user, String username, String nickname, String email) {
        return matchesLike(user.getUsername(), username)
                && matchesLike(user.getNickname(), nickname)
                && matchesLike(user.getEmail(), email);
    }

    private boolean matchesLike(String target, String filter) {
        if (filter == null) {
            return true;
        }
        if (target == null) {
            return false;
        }
        return target.toLowerCase().contains(filter.toLowerCase());
    }
}
