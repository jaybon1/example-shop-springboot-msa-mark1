package com.example.shop.payment.infrastructure.persistence.repository;

import com.example.shop.payment.infrastructure.persistence.entity.PaymentEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {
}
