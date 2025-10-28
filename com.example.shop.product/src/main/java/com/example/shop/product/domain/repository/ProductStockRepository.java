package com.example.shop.product.domain.repository;

import com.example.shop.product.domain.model.ProductStock;
import com.example.shop.product.domain.model.ProductStock.ProductStockType;
import java.util.UUID;

public interface ProductStockRepository {

    ProductStock save(ProductStock productStock);

    boolean existsByProductIdAndOrderIdAndType(UUID productId, UUID orderId, ProductStockType type);
}
