package com.example.shop.gateway.presentation.advice;

import lombok.Getter;

@Getter
public class GatewayException extends RuntimeException {

    private final GlobalError error;

    public GatewayException(GatewayError error) {
        super(error.getErrorMessage());
        this.error = error;
    }

    public GatewayException(GatewayError error, Throwable cause) {
        super(error.getErrorMessage(), cause);
        this.error = error;
    }
}
