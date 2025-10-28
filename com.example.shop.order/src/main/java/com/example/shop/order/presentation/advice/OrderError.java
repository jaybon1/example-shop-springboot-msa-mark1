package com.example.shop.order.presentation.advice;

import com.example.shop.global.presentation.advice.GlobalError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderError implements GlobalError {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 주문에 접근할 수 없습니다."),
    ORDER_ITEMS_EMPTY(HttpStatus.BAD_REQUEST, "주문 상품이 존재하지 않습니다."),
    ORDER_PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST, "주문 상품 정보를 찾을 수 없습니다."),
    ORDER_PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "상품 재고가 부족합니다."),
    ORDER_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "주문 수량이 올바르지 않습니다."),
    ORDER_AMOUNT_OVERFLOW(HttpStatus.BAD_REQUEST, "주문 금액 계산에 실패했습니다."),
    ORDER_BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 주문 요청입니다."),
    ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다.");

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
