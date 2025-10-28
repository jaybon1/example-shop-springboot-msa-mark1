package com.example.shop.order.presentation.advice;

import com.example.shop.global.presentation.advice.GlobalError;
import lombok.Getter;

@Getter
public class OrderException extends RuntimeException {

    private final GlobalError error;

    public OrderException(OrderError error) {
        super(error.getErrorMessage());
        this.error = error;
    }
}
