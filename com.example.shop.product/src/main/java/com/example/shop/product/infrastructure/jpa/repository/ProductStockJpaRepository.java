package com.example.shop.product.infrastructure.jpa.repository;

import com.example.shop.product.domain.model.ProductStock.ProductStockType;
import com.example.shop.product.infrastructure.jpa.entity.ProductStockEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStockJpaRepository extends JpaRepository<ProductStockEntity, UUID> {

    boolean existsByProductIdAndOrderIdAndType(UUID productId, UUID orderId, ProductStockType type);

    List<ProductStockEntity> findByOrderId(UUID orderId);

    boolean existsByOrderIdAndType(UUID orderId, ProductStockType type);

    List<ProductStockEntity> findByIdIn(List<UUID> productStockIdList);
}
