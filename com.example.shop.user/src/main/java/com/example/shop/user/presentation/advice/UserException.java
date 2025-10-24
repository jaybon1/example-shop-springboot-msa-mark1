package com.example.shop.user.presentation.advice;

import com.example.shop.global.presentation.advice.GlobalError;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException {

    private final GlobalError error;

    public UserException(UserError error) {
        super(error.getErrorMessage());
        this.error = error;
    }
}
