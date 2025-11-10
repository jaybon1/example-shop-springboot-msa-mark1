package com.example.shop.user.presentation.advice;

import com.example.shop.global.presentation.advice.GlobalExceptionHandler;
import com.example.shop.global.presentation.dto.ApiDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiDto<Object>> handleAuthException(AuthException exception) {
        return new ResponseEntity<>(
                ApiDto.builder()
                        .code(exception.getError().getErrorCode())
                        .message(exception.getError().getErrorMessage())
                        .build(),
                exception.getError().getHttpStatus()
        );
    }
}
