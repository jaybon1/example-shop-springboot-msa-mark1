package com.example.shop.order.presentation.dto.response;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.Order.Status;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedModel;

@Getter
@Builder
public class ResGetOrdersDtoV1 {

    private final OrderPageDto orderPage;

    public static ResGetOrdersDtoV1 of(Page<Order> orderPage) {
        return ResGetOrdersDtoV1.builder()
                .orderPage(new OrderPageDto(orderPage))
                .build();
    }

    @Getter
    @ToString
    public static class OrderPageDto extends PagedModel<OrderPageDto.OrderDto> {

        public OrderPageDto(Page<Order> orderPage) {
            super(
                    new PageImpl<>(
                            OrderDto.from(orderPage.getContent()),
                            orderPage.getPageable(),
                            orderPage.getTotalElements()
                    )
            );
        }

        @Getter
        @Builder
        public static class OrderDto {

            private final String id;
            private final Status status;
            private final Long totalAmount;
            private final Instant createdAt;
            private final Instant updatedAt;

            private static List<OrderDto> from(List<Order> orderList) {
                return orderList.stream()
                        .map(OrderDto::from)
                        .toList();
            }

            public static OrderDto from(Order order) {
                return OrderDto.builder()
                        .id(order.getId() != null ? order.getId().toString() : null)
                        .status(order.getStatus())
                        .totalAmount(order.getTotalAmount())
                        .createdAt(order.getCreatedAt())
                        .updatedAt(order.getUpdatedAt())
                        .build();
            }
        }
    }
}
