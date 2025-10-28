package com.example.shop.product.domain.repository;

import com.example.shop.product.domain.model.ProductStock;
import com.example.shop.product.domain.model.ProductStock.ProductStockType;
import java.util.List;
import java.util.UUID;

public interface ProductStockRepository {

    ProductStock save(ProductStock productStock);

    boolean existsByProductIdAndOrderIdAndType(UUID productId, UUID orderId, ProductStockType type);

    List<ProductStock> findByOrderId(UUID orderId);

    boolean existsByOrderIdAndType(UUID orderId, ProductStockType type);

    List<ProductStock> findByIdIn(List<UUID> productStockIdList);
}
