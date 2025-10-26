package com.example.shop.user.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class ReqPostAuthLoginDtoV1 {

    @NotNull(message = "회원 정보를 입력해주세요.")
    @Valid
    private UserDto user;

    @Getter
    @Builder
    public static class UserDto {

        @NotBlank(message = "아이디를 입력해주세요.")
        private String username;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;
    }
}
