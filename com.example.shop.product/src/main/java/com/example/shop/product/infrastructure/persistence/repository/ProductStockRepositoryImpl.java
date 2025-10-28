package com.example.shop.product.infrastructure.persistence.repository;

import com.example.shop.product.domain.model.ProductStock;
import com.example.shop.product.domain.model.ProductStock.ProductStockType;
import com.example.shop.product.domain.repository.ProductStockRepository;
import com.example.shop.product.infrastructure.persistence.entity.ProductStockEntity;
import com.example.shop.product.infrastructure.persistence.mapper.ProductStockMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductStockRepositoryImpl implements ProductStockRepository {

    private final ProductStockJpaRepository productStockJpaRepository;
    private final ProductStockMapper productStockMapper;

    @Override
    @Transactional
    public ProductStock save(ProductStock productStock) {
        ProductStockEntity entity = productStockMapper.toEntity(productStock);
        ProductStockEntity saved = productStockJpaRepository.save(entity);
        return productStockMapper.toDomain(saved);
    }

    @Override
    public boolean existsByProductIdAndOrderIdAndType(UUID productId, UUID orderId, ProductStockType type) {
        return productStockJpaRepository.existsByProductIdAndOrderIdAndType(productId, orderId, type);
    }

    @Override
    public List<ProductStock> findByOrderId(UUID orderId) {
        return productStockJpaRepository.findByOrderId(orderId)
                .stream()
                .map(productStockMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOrderIdAndType(UUID orderId, ProductStockType type) {
        return productStockJpaRepository.existsByOrderIdAndType(orderId, type);
    }

    @Override
    public List<ProductStock> findByIdIn(List<UUID> productStockIdList) {
        return productStockJpaRepository.findByIdIn(productStockIdList)
                .stream()
                .map(productStockMapper::toDomain)
                .toList();
    }
}
