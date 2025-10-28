package com.example.shop.product.infrastructure.persistence.entity;

import com.example.shop.global.infrastructure.persistence.entity.BaseEntity;
import com.example.shop.product.domain.model.ProductStock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(
        name = "PRODUCT_STOCK",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_stock_product_order_type",
                        columnNames = {"product_id", "order_id", "type"}
                )
        }
)
@DynamicInsert
@DynamicUpdate
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class ProductStockEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private ProductStock.ProductStockType type;
}
