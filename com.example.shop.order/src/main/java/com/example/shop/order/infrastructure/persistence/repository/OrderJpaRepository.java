package com.example.shop.order.infrastructure.persistence.repository;

import com.example.shop.order.infrastructure.persistence.entity.OrderEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    Page<OrderEntity> findByUserId(UUID userId, Pageable pageable);
}
