package com.example.shop.payment.infrastructure.persistence.repository;

import com.example.shop.payment.domain.model.Payment;
import com.example.shop.payment.domain.repository.PaymentRepository;
import com.example.shop.payment.infrastructure.persistence.entity.PaymentEntity;
import com.example.shop.payment.infrastructure.persistence.mapper.PaymentMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentEntity entity;
        if (payment.getId() != null) {
            entity = paymentJpaRepository.findById(payment.getId())
                    .orElseGet(() -> paymentMapper.toEntity(payment));
            paymentMapper.applyDomain(payment, entity);
        } else {
            entity = paymentMapper.toEntity(payment);
        }
        PaymentEntity saved = paymentJpaRepository.save(entity);
        return paymentMapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return paymentJpaRepository.findById(paymentId)
                .map(paymentMapper::toDomain);
    }
}
