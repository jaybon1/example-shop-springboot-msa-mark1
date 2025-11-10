package com.example.shop.order.presentation.advice;

import com.example.shop.global.presentation.advice.GlobalExceptionHandler;
import com.example.shop.global.presentation.dto.ApiDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ApiDto<Object>> handleOrderException(OrderException exception) {
        OrderError orderError = (OrderError) exception.getError();
        return ResponseEntity.status(orderError.getHttpStatus())
                .body(
                        ApiDto.builder()
                                .code(orderError.getErrorCode())
                                .message(orderError.getErrorMessage())
                                .build()
                );
    }
}
