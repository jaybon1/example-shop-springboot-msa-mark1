package com.example.shop.user.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class ReqPostAuthRegisterDtoV1 {

    @NotNull(message = "회원 정보를 입력해주세요.")
    @Valid
    private UserDto user;

    @Getter
    @Builder
    public static class UserDto {

        @NotBlank(message = "아이디를 입력해주세요.")
        @Pattern(regexp = "^[a-z0-9_]{3,20}$", message = "아이디는 3~20자의 영소문자, 숫자, 언더바(_)만으로 구성해주세요.")
        private String username;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;

        @NotBlank(message = "닉네임을 입력해주세요.")
        private String nickname;

        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;
    }
}
