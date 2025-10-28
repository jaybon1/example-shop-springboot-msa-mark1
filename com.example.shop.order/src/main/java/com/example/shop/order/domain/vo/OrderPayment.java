package com.example.shop.order.domain.vo;

import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(of = {"id", "status", "method", "amount"})
public class OrderPayment {

    private final UUID id;
    private final Status status;
    private final Method method;
    private final Long amount;

    public enum Status {
        REQUESTED,
        COMPLETED,
        CANCELLED
    }

    public enum Method {
        CARD,
        BANK_TRANSFER,
        MOBILE,
        POINT
    }
}
