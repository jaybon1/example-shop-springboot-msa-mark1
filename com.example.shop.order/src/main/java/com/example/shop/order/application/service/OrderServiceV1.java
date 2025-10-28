package com.example.shop.order.application.service;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.OrderItem;
import com.example.shop.order.domain.repository.OrderRepository;
import com.example.shop.order.presentation.advice.OrderError;
import com.example.shop.order.presentation.advice.OrderException;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResPostOrdersDtoV1;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceV1 {

    private final OrderRepository orderRepository;

    public ResGetOrdersDtoV1 getOrders(UUID authUserId, List<String> authUserRoleList, Pageable pageable) {
        if (pageable == null) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }

        Page<Order> orderPage;
        if (hasManagerPermission(authUserRoleList)) {
            orderPage = orderRepository.findAll(pageable);
        } else {
            UUID userId = requireAuthenticatedUser(authUserId);
            orderPage = orderRepository.findByUserId(userId, pageable);
        }
        return ResGetOrdersDtoV1.of(orderPage);
    }

    public ResGetOrderDtoV1 getOrder(UUID authUserId, List<String> authUserRoleList, UUID orderId) {
        Order order = findOrder(orderId);
        validateAccessPermission(order, authUserId, authUserRoleList);
        return ResGetOrderDtoV1.of(order);
    }

    @Transactional
    public ResPostOrdersDtoV1 postOrders(UUID authUserId, List<String> authUserRoleList, ReqPostOrdersDtoV1 reqDto) {
        UUID userId = requireAuthenticatedUser(authUserId);
        validateCreateRequest(reqDto);

        ReqPostOrdersDtoV1.OrderDto reqOrder = reqDto.getOrder();
        List<OrderItem> orderItemList = new ArrayList<>();
        long totalAmount = 0L;
        for (ReqPostOrdersDtoV1.OrderDto.OrderItemDto itemDto : reqOrder.getOrderItemList()) {
            UUID productId = itemDto.getProductId();
            Long quantityValue = itemDto.getQuantity();
            if (productId == null || quantityValue == null || quantityValue <= 0) {
                throw new OrderException(OrderError.ORDER_INVALID_QUANTITY);
            }

            long quantity = quantityValue;
            long unitPrice = 0L; // TODO: product service 연동 시 실제 가격 정보로 대체
            long lineTotal = safeMultiply(unitPrice, quantity);
            totalAmount = safeAdd(totalAmount, lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(productId)
                    .productName(null)
                    .unitPrice(unitPrice)
                    .quantity(quantity)
                    .lineTotal(lineTotal)
                    .build();
            orderItemList.add(orderItem);
        }

        Order order = Order.builder()
                .userId(userId)
                .status(Order.Status.CREATED)
                .totalAmount(totalAmount)
                .orderItemList(List.copyOf(orderItemList))
                .createdAt(Instant.now())
                .build();

        Order savedOrder = orderRepository.save(order);
        return ResPostOrdersDtoV1.of(savedOrder);
    }

    @Transactional
    public void cancelOrder(UUID authUserId, List<String> authUserRoleList, UUID orderId) {
        Order order = findOrder(orderId);
        validateAccessPermission(order, authUserId, authUserRoleList);

        if (Order.Status.CANCELLED.equals(order.getStatus())) {
            throw new OrderException(OrderError.ORDER_ALREADY_CANCELLED);
        }

        Order cancelledOrder = order.markCancelled();
        orderRepository.save(cancelledOrder);
    }

    private void validateCreateRequest(ReqPostOrdersDtoV1 reqDto) {
        if (reqDto == null || reqDto.getOrder() == null) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }
        if (CollectionUtils.isEmpty(reqDto.getOrder().getOrderItemList())) {
            throw new OrderException(OrderError.ORDER_ITEMS_EMPTY);
        }
    }

    private Order findOrder(UUID orderId) {
        if (orderId == null) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderError.ORDER_NOT_FOUND));
    }

    private void validateAccessPermission(Order order, UUID authUserId, List<String> authUserRoleList) {
        if (hasManagerPermission(authUserRoleList)) {
            return;
        }
        UUID userId = requireAuthenticatedUser(authUserId);
        if (!order.isOwnedBy(userId)) {
            throw new OrderException(OrderError.ORDER_FORBIDDEN);
        }
    }

    private boolean hasManagerPermission(List<String> authUserRoleList) {
        if (CollectionUtils.isEmpty(authUserRoleList)) {
            return false;
        }
        return authUserRoleList.stream()
                .filter(Objects::nonNull)
                .anyMatch(role -> role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("MANAGER"));
    }

    private UUID requireAuthenticatedUser(UUID authUserId) {
        if (authUserId == null) {
            throw new OrderException(OrderError.ORDER_FORBIDDEN);
        }
        return authUserId;
    }

    private long safeAdd(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException ex) {
            throw new OrderException(OrderError.ORDER_AMOUNT_OVERFLOW);
        }
    }

    private long safeMultiply(long left, long right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException ex) {
            throw new OrderException(OrderError.ORDER_AMOUNT_OVERFLOW);
        }
    }
}
