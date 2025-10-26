package com.example.shop.user.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReqPostAuthInvalidateBeforeTokenDtoV1 {

    @NotNull(message = "회원 정보를 입력해주세요.")
    @Valid
    private UserDto user;

    @Getter
    @Builder
    public static class UserDto {

        @NotNull(message = "유저 고유번호를 입력해주세요.")
        private UUID id;

    }

}
