package com.example.shop.product.infrastructure.persistence.repository;

import com.example.shop.product.domain.model.Product;
import com.example.shop.product.domain.repository.ProductRepository;
import com.example.shop.product.infrastructure.persistence.entity.ProductEntity;
import com.example.shop.product.infrastructure.persistence.mapper.ProductMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public Product save(Product product) {
        ProductEntity productEntity;
        if (product.getId() != null) {
            productEntity = productJpaRepository.findById(product.getId())
                    .orElseGet(() -> productMapper.toEntity(product));
            productMapper.applyDomain(product, productEntity);
        } else {
            productEntity = productMapper.toEntity(product);
        }
        ProductEntity saved = productJpaRepository.save(productEntity);
        return productMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return productJpaRepository.findById(productId)
                .map(productMapper::toDomain);
    }

    @Override
    public Optional<Product> findByName(String name) {
        return productJpaRepository.findByName(name)
                .map(productMapper::toDomain);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productJpaRepository.findAll(pageable)
                .map(productMapper::toDomain);
    }

    @Override
    public Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        return productJpaRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(productMapper::toDomain);
    }

    @Override
    public List<Product> findAllById(Iterable<UUID> productIdList) {
        return productJpaRepository.findAllById(productIdList)
                .stream()
                .map(productMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByIdIn(List<UUID> productIdList) {
        return productJpaRepository.findByIdIn(productIdList)
                .stream()
                .map(productMapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return productJpaRepository.count();
    }
}
