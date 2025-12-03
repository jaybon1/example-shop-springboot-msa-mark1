package com.example.shop.product.infrastructure.jpa.mapper;

import com.example.shop.product.domain.model.ProductStock;
import com.example.shop.product.infrastructure.jpa.entity.ProductStockEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductStockMapper {

    public ProductStock toDomain(ProductStockEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductStock.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .orderId(entity.getOrderId())
                .quantity(entity.getQuantity())
                .type(entity.getType())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public ProductStockEntity toEntity(ProductStock domain) {
        if (domain == null) {
            return null;
        }
        return ProductStockEntity.builder()
                .id(domain.getId())
                .productId(domain.getProductId())
                .orderId(domain.getOrderId())
                .quantity(domain.getQuantity())
                .type(domain.getType())
                .build();
    }
}
