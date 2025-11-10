package com.example.shop.payment.presentation.advice;

import com.example.shop.global.presentation.advice.GlobalExceptionHandler;
import com.example.shop.global.presentation.dto.ApiDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiDto<Object>> handlePaymentException(PaymentException exception) {
        PaymentError paymentError = (PaymentError) exception.getError();
        return ResponseEntity.status(paymentError.getHttpStatus())
                .body(
                        ApiDto.builder()
                                .code(paymentError.getErrorCode())
                                .message(paymentError.getErrorMessage())
                                .build()
                );
    }
}
