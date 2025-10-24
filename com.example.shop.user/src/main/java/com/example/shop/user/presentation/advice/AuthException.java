package com.example.shop.user.presentation.advice;

import com.example.shop.global.presentation.advice.GlobalError;
import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private final GlobalError error;

    public AuthException(AuthError error) {
        super(error.getErrorMessage());
        this.error = error;
    }
}
