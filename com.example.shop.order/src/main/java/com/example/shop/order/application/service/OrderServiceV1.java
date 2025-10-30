package com.example.shop.order.application.service;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.OrderItem;
import com.example.shop.order.domain.repository.OrderRepository;
import com.example.shop.order.domain.vo.OrderPayment;
import com.example.shop.order.infrastructure.resttemplate.product.client.ProductRestTemplateClientV1;
import com.example.shop.order.infrastructure.resttemplate.product.dto.request.ReqPostInternalProductsReleaseStockDtoV1;
import com.example.shop.order.infrastructure.resttemplate.product.dto.response.ResGetProductDtoV1;
import com.example.shop.order.presentation.advice.OrderError;
import com.example.shop.order.presentation.advice.OrderException;
import com.example.shop.order.presentation.dto.request.ReqPostInternalOrderCompleteDtoV1;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResPostOrdersDtoV1;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final ProductRestTemplateClientV1 productRestTemplateClientV1;

    public ResGetOrdersDtoV1 getOrders(UUID authUserId, List<String> authUserRoleList, Pageable pageable) {
        if (pageable == null) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }

        Page<Order> orderPage;
        if (hasManagerPermission(authUserRoleList)) {
            orderPage = orderRepository.findAll(pageable);
        } else {
            orderPage = orderRepository.findByUserId(authUserId, pageable);
        }
        return ResGetOrdersDtoV1.of(orderPage);
    }

    public ResGetOrderDtoV1 getOrder(UUID authUserId, List<String> authUserRoleList, UUID orderId) {
        Order order = findOrder(orderId);
        validateAccessPermission(order, authUserId, authUserRoleList);
        return ResGetOrderDtoV1.of(order);
    }

    @Transactional
    public ResPostOrdersDtoV1 postOrders(UUID authUserId, ReqPostOrdersDtoV1 reqDto) {
        if (reqDto == null || reqDto.getOrder() == null) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }
        ReqPostOrdersDtoV1.OrderDto reqOrder = reqDto.getOrder();
        if (CollectionUtils.isEmpty(reqOrder.getOrderItemList())) {
            throw new OrderException(OrderError.ORDER_ITEMS_EMPTY);
        }
        List<OrderItem> orderItemList = new ArrayList<>();
        Map<UUID, Long> productQuantityMap = new LinkedHashMap<>();
        Map<UUID, ResGetProductDtoV1.ProductDto> productCache = new HashMap<>();
        long totalAmount = 0L;
        for (ReqPostOrdersDtoV1.OrderDto.OrderItemDto itemDto : reqOrder.getOrderItemList()) {
            UUID productId = itemDto.getProductId();
            Long quantityValue = itemDto.getQuantity();
            if (productId == null || quantityValue == null || quantityValue <= 0) {
                throw new OrderException(OrderError.ORDER_INVALID_QUANTITY);
            }

            long quantity = quantityValue;
            ResGetProductDtoV1.ProductDto productDto = productCache.computeIfAbsent(productId, this::fetchProduct);
            Long unitPriceValue = productDto.getPrice();
            if (unitPriceValue == null || unitPriceValue < 0) {
                throw new OrderException(OrderError.ORDER_BAD_REQUEST);
            }
            long unitPrice = unitPriceValue;
            long lineTotal = safeMultiply(unitPrice, quantity);
            totalAmount = safeAdd(totalAmount, lineTotal);
            long aggregatedQuantity = productQuantityMap.containsKey(productId)
                    ? safeAdd(productQuantityMap.get(productId), quantity)
                    : quantity;
            productQuantityMap.put(productId, aggregatedQuantity);

            OrderItem orderItem = OrderItem.builder()
                    .productId(productId)
                    .productName(productDto.getName())
                    .unitPrice(unitPrice)
                    .quantity(quantity)
                    .lineTotal(lineTotal)
                    .build();
            orderItemList.add(orderItem);
        }

        Order order = Order.builder()
                .userId(authUserId)
                .status(Order.Status.CREATED)
                .totalAmount(totalAmount)
                .orderItemList(List.copyOf(orderItemList))
                .createdAt(Instant.now())
                .build();

        Order savedOrder = orderRepository.save(order);
        productRestTemplateClientV1.postInternalProductsReleaseStock(
                buildReleaseStockRequest(savedOrder.getId(), productQuantityMap)
        );
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

    @Transactional
    public void postInternalOrdersComplete(UUID orderId, ReqPostInternalOrderCompleteDtoV1 reqDto) {
        if (reqDto == null || reqDto.getPayment() == null) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }

        Order order = findOrder(orderId);

        if (Order.Status.CANCELLED.equals(order.getStatus())) {
            throw new OrderException(OrderError.ORDER_ALREADY_CANCELLED);
        }
        if (Order.Status.PAID.equals(order.getStatus())) {
            throw new OrderException(OrderError.ORDER_ALREADY_PAID);
        }

        ReqPostInternalOrderCompleteDtoV1.PaymentDto paymentDto = reqDto.getPayment();
        if (paymentDto.getPaymentId() == null || paymentDto.getMethod() == null) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }
        if (paymentDto.getAmount() == null || !Objects.equals(order.getTotalAmount(), paymentDto.getAmount())) {
            throw new OrderException(OrderError.ORDER_PAYMENT_AMOUNT_MISMATCH);
        }

        OrderPayment orderPayment = OrderPayment.builder()
                .id(paymentDto.getPaymentId())
                .status(OrderPayment.Status.COMPLETED)
                .method(paymentDto.getMethod())
                .amount(paymentDto.getAmount())
                .build();

        Order completedOrder = order.markPaid(orderPayment);
        orderRepository.save(completedOrder);
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderError.ORDER_NOT_FOUND));
    }

    private void validateAccessPermission(Order order, UUID authUserId, List<String> authUserRoleList) {
        if (hasManagerPermission(authUserRoleList)) {
            return;
        }
        if (!order.isOwnedBy(authUserId)) {
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

    private ResGetProductDtoV1.ProductDto fetchProduct(UUID productId) {
        ResGetProductDtoV1 response = productRestTemplateClientV1.getProduct(productId);
        if (response == null || response.getProduct() == null) {
            throw new OrderException(OrderError.ORDER_PRODUCT_NOT_FOUND);
        }
        return response.getProduct();
    }

    private ReqPostInternalProductsReleaseStockDtoV1 buildReleaseStockRequest(UUID orderId, Map<UUID, Long> productQuantityMap) {
        return ReqPostInternalProductsReleaseStockDtoV1.builder()
                .order(
                        ReqPostInternalProductsReleaseStockDtoV1.OrderDto.builder()
                                .orderId(orderId)
                                .build()
                )
                .productStocks(
                        productQuantityMap.entrySet()
                                .stream()
                                .map(entry -> ReqPostInternalProductsReleaseStockDtoV1.ProductStockDto.builder()
                                        .productId(entry.getKey())
                                        .quantity(entry.getValue())
                                        .build())
                                .toList()
                )
                .build();
    }
}
