package com.example.shop.product.application.service;

import com.example.shop.product.domain.model.Product;
import com.example.shop.product.domain.repository.ProductRepository;
import com.example.shop.product.presentation.advice.ProductError;
import com.example.shop.product.presentation.advice.ProductException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceV1 {

    private final ProductRepository productRepository;

    public Page<Product> getProducts(Pageable pageable, String name) {
        String normalizedName = normalize(name);
        if (normalizedName == null) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByNameContainingIgnoreCase(normalizedName, pageable);
    }

    public Product getProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductError.PRODUCT_CAN_NOT_FOUND));
    }

    @Transactional
    public Product createProduct(String name, Long price, Long stock) {
        String normalizedName = normalize(name);
        if (normalizedName == null) {
            throw new ProductException(ProductError.PRODUCT_BAD_REQUEST);
        }

        validateDuplicatedName(normalizedName, Optional.empty());

        Product newProduct = Product.builder()
                .name(normalizedName)
                .price(price)
                .stock(stock)
                .build();

        return productRepository.save(newProduct);
    }

    private void validateDuplicatedName(String name, Optional<UUID> excludeId) {
        productRepository.findByName(name)
                .ifPresent(product -> {
                    if (excludeId.isEmpty() || !product.getId().equals(excludeId.get())) {
                        throw new ProductException(ProductError.PRODUCT_NAME_DUPLICATED);
                    }
                });
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
