package com.community.soap.ordering.application.response;

import com.community.soap.ordering.domain.entity.Order;
import com.community.soap.ordering.domain.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record GetOrderResponse(
        Long orderId,
        OrderStatus orderStatus,
        Integer totalAmount,
        LocalDateTime createdAt,
        List<Line> items
) {

    public static GetOrderResponse from(Order order) {
        return GetOrderResponse.builder()
                .orderId(order.getOrderId())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(Line.from(order))
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record Line(
            Long orderItemId,
            Long productId,
            String skuCode,
            String name,
            Integer unitPrice,
            Integer quantity,
            Integer lineAmount
    ) {

        public static List<Line> from(Order order) {
            return order.getOrderItems().stream()
                    .map(li ->
                            Line.builder()
                                    .orderItemId(li.getOrderItemId())
                                    .productId(li.getProductId())
                                    .skuCode(li.getSkuCode())
                                    .name(li.getProductName())
                                    .unitPrice(li.getUnitPrice())
                                    .quantity(li.getQuantity())
                                    .lineAmount(li.getLineAmount())
                                    .build()
                    )
                    .toList();
        }
    }
}
