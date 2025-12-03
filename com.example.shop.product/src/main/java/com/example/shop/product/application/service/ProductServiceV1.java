package com.example.shop.product.application.service;

import com.example.shop.product.domain.model.Product;
import com.example.shop.product.domain.model.ProductStock;
import com.example.shop.product.domain.model.ProductStock.ProductStockType;
import com.example.shop.product.domain.repository.ProductRepository;
import com.example.shop.product.domain.repository.ProductStockRepository;
import com.example.shop.product.presentation.advice.ProductError;
import com.example.shop.product.presentation.advice.ProductException;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductsReleaseStockDtoV1;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductsReturnStockDtoV1;
import com.example.shop.product.presentation.dto.request.ReqPostProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostProductsDtoV1;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceV1 {

    private static final String PRODUCT_CACHE_NAME = "product";

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;

    private final CacheManager cacheManager;

    public ResGetProductsDtoV1 getProducts(Pageable pageable, String name) {
        String normalizedName = normalize(name);
        Page<Product> productPage = normalizedName == null
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(normalizedName, pageable);
        return ResGetProductsDtoV1.of(productPage);
    }

    @Cacheable(cacheNames = PRODUCT_CACHE_NAME, key = "#productId")
    public ResGetProductDtoV1 getProduct(UUID productId) {
        return ResGetProductDtoV1.of(findProductById(productId));
    }

    @Transactional
    public ResPostProductsDtoV1 postProducts(ReqPostProductsDtoV1 reqDto) {
        ReqPostProductsDtoV1.ProductDto reqProduct = reqDto.getProduct();
        String normalizedName = normalize(reqProduct.getName());
        if (normalizedName == null) {
            throw new ProductException(ProductError.PRODUCT_BAD_REQUEST);
        }
        validateDuplicatedName(normalizedName, Optional.empty());
        Product newProduct = Product.builder()
                .name(normalizedName)
                .price(reqProduct.getPrice())
                .stock(reqProduct.getStock())
                .build();
        Product savedProduct = productRepository.save(newProduct);
        return ResPostProductsDtoV1.of(savedProduct);
    }

    @Transactional
    public void postInternalProductsReleaseStock(ReqPostInternalProductsReleaseStockDtoV1 reqDto) {
        if (productStockRepository.existsByOrderIdAndType(reqDto.getOrder().getOrderId(), ProductStockType.RELEASE)) {
            throw new ProductException(ProductError.PRODUCT_BAD_REQUEST);
        }

        List<UUID> productIds = reqDto.getProductStocks().stream()
                .map(ReqPostInternalProductsReleaseStockDtoV1.ProductStockDto::getProductId)
                .toList();

        List<Product> productList = productRepository.findByIdIn(productIds);

        reqDto.getProductStocks().forEach(productStockDto -> {
            productList.stream().filter(product -> product.getId().equals(productStockDto.getProductId())).findFirst()
                    .ifPresent(product -> {
                        if (product.getStock() < productStockDto.getQuantity()) {
                            throw new ProductException(ProductError.PRODUCT_BAD_REQUEST);
                        }
                        Product updatedProduct = product.update(null, null, product.getStock() - productStockDto.getQuantity());
                        productRepository.save(updatedProduct);
                    });
        });

        reqDto.getProductStocks().forEach(productStockDto ->
                productStockRepository.save(
                        ProductStock.builder()
                                .productId(productStockDto.getProductId())
                                .orderId(reqDto.getOrder().getOrderId())
                                .quantity(productStockDto.getQuantity())
                                .type(ProductStockType.RELEASE)
                                .build()
                )
        );

        evictProductCache(productIds);
    }

    @Transactional
    public void postInternalProductsReturnStock(ReqPostInternalProductsReturnStockDtoV1 reqDto) {
        if (productStockRepository.existsByOrderIdAndType(reqDto.getOrder().getOrderId(), ProductStockType.RETURN)) {
            throw new ProductException(ProductError.PRODUCT_BAD_REQUEST);
        }

        List<ProductStock> productStockList = productStockRepository.findByOrderId(reqDto.getOrder().getOrderId());
        List<UUID> productIds = productStockList.stream()
                .map(ProductStock::getProductId)
                .toList();

        List<Product> productList = productRepository.findByIdIn(productIds);

        productStockList.forEach(productStock -> {
            productList.stream().filter(product -> product.getId().equals(productStock.getProductId())).findFirst()
                    .ifPresent(product -> {
                        Product updatedProduct = product.update(null, null, product.getStock() + productStock.getQuantity());
                        productRepository.save(updatedProduct);
                    });
        });

        productStockList.forEach(productStock ->
                productStockRepository.save(
                        ProductStock.builder()
                                .productId(productStock.getProductId())
                                .orderId(reqDto.getOrder().getOrderId())
                                .quantity(productStock.getQuantity())
                                .type(ProductStockType.RETURN)
                                .build()
                )
        );

        evictProductCache(productIds);
    }

    private void evictProductCache(List<UUID> productIds) {
        Cache cache = cacheManager.getCache(PRODUCT_CACHE_NAME);
        if (cache == null) {
            return;
        }
        productIds.forEach(cache::evict);
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

    private Product findProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductError.PRODUCT_CAN_NOT_FOUND));
    }

}
