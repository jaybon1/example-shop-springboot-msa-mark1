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
    ORDER_PRODUCT_CAN_NOT_FOUND(HttpStatus.BAD_REQUEST, "주문 상품 정보를 찾을 수 없습니다."),
    ORDER_PRODUCT_DUPLICATED(HttpStatus.BAD_REQUEST, "중복된 상품이 존재합니다."),
    ORDER_PRODUCT_STOCK_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "상품 재고가 부족합니다."),
    ORDER_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "주문 수량이 올바르지 않습니다."),
    ORDER_AMOUNT_OVERFLOW(HttpStatus.BAD_REQUEST, "주문 금액 계산에 실패했습니다."),
    ORDER_BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 주문 요청입니다."),
    ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다."),
    ORDER_ALREADY_PAID(HttpStatus.BAD_REQUEST, "이미 결제가 완료된 주문입니다."),
    ORDER_PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),
    ORDER_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    ORDER_PAYMENT_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 결제입니다."),
    ORDER_PRODUCT_HTTP_ERROR(HttpStatus.BAD_REQUEST, "상품 서비스 HTTP 응답 처리 중 오류가 발생했습니다."),
    ORDER_PRODUCT_REST_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "상품 서비스 호출 중 오류가 발생했습니다."),
    ORDER_PAYMENT_HTTP_ERROR(HttpStatus.BAD_REQUEST, "결제 서비스 HTTP 응답 처리 중 오류가 발생했습니다."),
    ORDER_PAYMENT_REST_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "결제 서비스 호출 중 오류가 발생했습니다.");

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
