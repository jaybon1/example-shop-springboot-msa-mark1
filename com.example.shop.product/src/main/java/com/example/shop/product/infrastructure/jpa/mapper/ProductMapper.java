package com.example.shop.product.infrastructure.jpa.mapper;

import com.example.shop.product.domain.model.Product;
import com.example.shop.product.infrastructure.jpa.entity.ProductEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toDomain(ProductEntity productEntity) {
        if (productEntity == null) {
            return null;
        }
        return Product.builder()
                .id(productEntity.getId())
                .name(productEntity.getName())
                .price(productEntity.getPrice())
                .stock(productEntity.getStock())
                .createdAt(productEntity.getCreatedAt())
                .createdBy(productEntity.getCreatedBy())
                .updatedAt(productEntity.getUpdatedAt())
                .updatedBy(productEntity.getUpdatedBy())
                .deletedAt(productEntity.getDeletedAt())
                .deletedBy(productEntity.getDeletedBy())
                .build();
    }

    public ProductEntity toEntity(Product product) {
        if (product == null) {
            return null;
        }
        return ProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }

    public void applyDomain(Product product, ProductEntity productEntity) {
        if (product == null || productEntity == null) {
            return;
        }
        productEntity.update(product.getName(), product.getPrice(), product.getStock());
        if (product.getDeletedAt() != null) {
            Optional.ofNullable(toUuid(product.getDeletedBy()))
                    .ifPresent(uuid -> productEntity.markDeleted(product.getDeletedAt(), uuid));
        }
    }

    private UUID toUuid(String value) {
        try {
            return value != null ? UUID.fromString(value) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
