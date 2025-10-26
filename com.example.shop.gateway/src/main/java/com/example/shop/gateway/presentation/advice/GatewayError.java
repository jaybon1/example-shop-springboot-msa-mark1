package com.example.shop.gateway.presentation.advice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GatewayError implements GlobalError {

    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었거나 유효하지 않습니다."),
    TOKEN_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "토큰 검증 서비스에 연결할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

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
        return message;
    }
}
