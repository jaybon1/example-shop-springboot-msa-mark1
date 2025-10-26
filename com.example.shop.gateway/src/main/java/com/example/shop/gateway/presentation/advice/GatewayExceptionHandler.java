package com.example.shop.gateway.presentation.advice;

import com.example.shop.gateway.presentation.dto.ApiDto;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Order(0)
public class GatewayExceptionHandler {

    @ExceptionHandler(GatewayException.class)
    public Mono<ResponseEntity<ApiDto<Object>>> handleGatewayException(GatewayException exception) {
        return Mono.just(
                ResponseEntity
                        .status(exception.getError().getHttpStatus())
                        .body(ApiDto.builder()
                                .code(exception.getError().getErrorCode())
                                .message(exception.getError().getErrorMessage())
                                .build())
        );
    }
}
