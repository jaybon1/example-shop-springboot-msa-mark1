package com.example.shop.user.presentation.advice;

import com.example.shop.global.presentation.advice.GlobalError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthError implements GlobalError {

    AUTH_USER_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 존재하는 아이디입니다."),
    AUTH_USERNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "아이디를 정확히 입력해주세요."),
    AUTH_PASSWORD_NOT_MATCHED(HttpStatus.BAD_REQUEST, "비밀번호를 정확히 입력해주세요."),
    AUTH_USER_CAN_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 유저를 찾을 수 없습니다."),
    AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getErrorCode() {
        return name();
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
