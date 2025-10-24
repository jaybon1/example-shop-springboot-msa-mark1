package com.example.shop.user.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.user.application.service.UserServiceV1;
import com.example.shop.user.infrastructure.config.security.auth.CustomUserDetails;
import com.example.shop.user.presentation.dto.response.ResGetUsersDtoV1;
import com.example.shop.user.presentation.dto.response.ResGetUserDtoV1;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/v1/users")
public class UserControllerV1 {

    private final UserServiceV1 userService;

    public UserControllerV1(UserServiceV1 userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiDto<ResGetUsersDtoV1>> getUsers(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault Pageable pageable,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "email", required = false) String email
    ) {
        UUID authUserId = currentUser != null ? currentUser.getId() : null;
        List<String> authUserRoleList = currentUser != null ? currentUser.getRoleList() : List.of();
        ResGetUsersDtoV1 responseBody = userService.getUsers(authUserId, authUserRoleList, pageable, username, nickname, email);

        return ResponseEntity.ok(
                ApiDto.<ResGetUsersDtoV1>builder()
                        .message("사용자 목록 조회가 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetUserDtoV1>> getUser(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("id") UUID userId
    ) {
        UUID authUserId = currentUser != null ? currentUser.getId() : null;
        List<String> authUserRoleList = currentUser != null ? currentUser.getRoleList() : List.of();
        ResGetUserDtoV1 responseBody = userService.getUser(authUserId, authUserRoleList, userId);

        return ResponseEntity.ok(
                ApiDto.<ResGetUserDtoV1>builder()
                        .message("사용자 조회가 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiDto<Object>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("id") UUID userId
    ) {
        UUID authUserId = currentUser != null ? currentUser.getId() : null;
        List<String> authUserRoleList = currentUser != null ? currentUser.getRoleList() : List.of();
        userService.deleteUser(authUserId, authUserRoleList, userId);
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message(userId + " 사용자가 삭제되었습니다.")
                        .build()
        );
    }
}
