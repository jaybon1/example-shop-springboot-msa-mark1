package com.example.shop.order.presentation.dto.response;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.OrderItem;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostOrdersDtoV1 {

    private final OrderDto order;

    public static ResPostOrdersDtoV1 of(Order order) {
        return ResPostOrdersDtoV1.builder()
                .order(OrderDto.from(order))
                .build();
    }

    @Getter
    @Builder
    public static class OrderDto {

        private final String id;

        public static OrderDto from(Order order) {
            return OrderDto.builder()
                    .id(String.valueOf(order.getId()))
                    .build();
        }
    }
}
