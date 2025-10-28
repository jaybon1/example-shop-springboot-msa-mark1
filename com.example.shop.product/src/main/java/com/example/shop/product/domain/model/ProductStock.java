package com.example.shop.product.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class ProductStock {

    private final UUID id;
    private final UUID productId;
    private final UUID orderId;
    private final Long quantity;
    private final ProductStockType type;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant updatedAt;
    private final String updatedBy;

    public enum ProductStockType {
        RELEASE,
        RETURN
    }
}
