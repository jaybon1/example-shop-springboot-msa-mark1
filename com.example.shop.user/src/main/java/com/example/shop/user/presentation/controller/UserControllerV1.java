package com.example.shop.user.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.user.presentation.dto.response.ResGetUsersDtoV1;
import com.example.shop.user.presentation.dto.response.ResGetUsersWithIdDtoV1;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
public class UserControllerV1 {

    @GetMapping
    public ResponseEntity<ApiDto<ResGetUsersDtoV1>> getUsers() {
        List<ResGetUsersDtoV1.UserDto> userDtoList = List.of(
                ResGetUsersDtoV1.UserDto.builder()
                        .id(UUID.fromString("11111111-1111-1111-1111-111111111111").toString())
                        .username("admin")
                        .nickname("관리자")
                        .email("admin@example.com")
                        .build(),
                ResGetUsersDtoV1.UserDto.builder()
                        .id(UUID.fromString("22222222-2222-2222-2222-222222222222").toString())
                        .username("jane")
                        .nickname("제인")
                        .email("jane@example.com")
                        .build()
        );

        ResGetUsersDtoV1 responseBody = ResGetUsersDtoV1.builder()
                .userList(userDtoList)
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResGetUsersDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetUsersWithIdDtoV1>> getUser(@PathVariable("id") UUID userId) {
        ResGetUsersWithIdDtoV1 responseBody = ResGetUsersWithIdDtoV1.builder()
                .user(
                        ResGetUsersWithIdDtoV1.UserDto.builder()
                                .id(userId.toString())
                                .username("dummy")
                                .nickname("더미 유저")
                                .email("dummy@example.com")
                                .build()
                )
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResGetUsersWithIdDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiDto<Object>> deleteUser(@PathVariable("id") UUID userId) {
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message(userId + " 사용자가 삭제되었습니다.")
                        .build()
        );
    }
}
