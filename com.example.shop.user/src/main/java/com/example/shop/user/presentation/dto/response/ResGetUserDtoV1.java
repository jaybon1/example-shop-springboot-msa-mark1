package com.example.shop.user.presentation.dto.response;

import com.example.shop.user.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResGetUserDtoV1 {

    private final UserDto user;

    public static ResGetUserDtoV1 of(User user) {
        return ResGetUserDtoV1.builder()
                .user(UserDto.from(user))
                .build();
    }

    @Getter
    @Builder
    public static class UserDto {

        private final String id;
        private final String username;
        private final String nickname;
        private final String email;

        public static UserDto from(User user) {
            return UserDto.builder()
                    .id(user.getId().toString())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .build();
        }

    }

}
