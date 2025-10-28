package com.example.shop.order.infrastructure.persistence.repository;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.repository.OrderRepository;
import com.example.shop.order.infrastructure.persistence.entity.OrderEntity;
import com.example.shop.order.infrastructure.persistence.mapper.OrderMapper;
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
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity orderEntity;
        if (order.getId() != null) {
            orderEntity = orderJpaRepository.findById(order.getId())
                    .orElseGet(() -> orderMapper.toEntity(order));
            orderMapper.applyDomain(order, orderEntity);
        } else {
            orderEntity = orderMapper.toEntity(order);
        }
        OrderEntity saved = orderJpaRepository.save(orderEntity);
        return orderMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findById(orderId)
                .map(orderMapper::toDomain);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return orderJpaRepository.findAll(pageable)
                .map(orderMapper::toDomain);
    }

    @Override
    public Page<Order> findByUserId(UUID userId, Pageable pageable) {
        return orderJpaRepository.findByUserId(userId, pageable)
                .map(orderMapper::toDomain);
    }
}
