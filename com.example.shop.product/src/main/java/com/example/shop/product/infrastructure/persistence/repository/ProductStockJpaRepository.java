package com.example.shop.product.infrastructure.persistence.repository;

import com.example.shop.product.domain.model.ProductStock.ProductStockType;
import com.example.shop.product.infrastructure.persistence.entity.ProductStockEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStockJpaRepository extends JpaRepository<ProductStockEntity, UUID> {

    boolean existsByProductIdAndOrderIdAndType(UUID productId, UUID orderId, ProductStockType type);
}
