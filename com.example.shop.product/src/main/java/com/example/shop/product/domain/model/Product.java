package com.example.shop.product.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class Product {

    private final UUID id;
    private final String name;
    private final Long price;
    private final Long stock;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant updatedAt;
    private final String updatedBy;
    private final Instant deletedAt;
    private final String deletedBy;

    public Product update(String name, Long price, Long stock) {
        return toBuilder()
                .name(name != null ? name : this.name)
                .price(price != null ? price : this.price)
                .stock(stock != null ? stock : this.stock)
                .build();
    }

    public Product markDeleted(Instant deletedAt, UUID userId) {
        return toBuilder()
                .deletedAt(deletedAt)
                .deletedBy(userId != null ? userId.toString() : null)
                .build();
    }

    private ProductBuilder toBuilder() {
        return Product.builder()
                .id(id)
                .name(name)
                .price(price)
                .stock(stock)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .deletedAt(deletedAt)
                .deletedBy(deletedBy);
    }
}
