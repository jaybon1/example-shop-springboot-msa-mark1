package com.example.shop.user.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResGetUserDtoV1 {

    private final UserDto user;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserDto {
        private final String id;
        private final String username;
        private final String nickname;
        private final String email;
    }
}
