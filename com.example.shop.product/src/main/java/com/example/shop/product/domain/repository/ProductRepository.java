package com.example.shop.product.domain.repository;

import com.example.shop.product.domain.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID productId);

    Optional<Product> findByName(String name);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Product> findAllById(Iterable<UUID> productIdList);

    List<Product> findByIdIn(List<UUID> productIdList);

    long count();
}
