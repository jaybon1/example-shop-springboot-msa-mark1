package com.example.shop.product.presentation.advice;

import com.example.shop.global.presentation.advice.GlobalError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductError implements GlobalError {

    PRODUCT_CAN_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 상품을 찾을 수 없습니다."),
    PRODUCT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    PRODUCT_FORBIDDEN(HttpStatus.FORBIDDEN, "상품에 대한 권한이 없습니다."),
    PRODUCT_NAME_DUPLICATED(HttpStatus.BAD_REQUEST, "이미 등록된 상품명입니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

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
        return errorMessage;
    }
}
