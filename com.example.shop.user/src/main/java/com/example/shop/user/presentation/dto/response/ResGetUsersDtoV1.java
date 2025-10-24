package com.example.shop.user.presentation.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResGetUsersDtoV1 {

    private final List<UserDto> users;

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
