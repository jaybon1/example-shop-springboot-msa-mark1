package com.example.shop.payment.presentation.advice;

import com.example.shop.global.presentation.dto.ApiDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class PaymentExceptionHandler {

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
