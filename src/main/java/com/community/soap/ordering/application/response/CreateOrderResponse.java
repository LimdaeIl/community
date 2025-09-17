package com.community.soap.ordering.application.response;

import com.community.soap.ordering.domain.entity.OrderStatus;
import java.util.List;

public record CreateOrderResponse(
        Long orderId,
        OrderStatus orderStatus,
        Integer totalAmount,
        List<Line> items
) {
    public record Line(
            Long orderItemId,
            Long productId,
            String skuCode,
            String name,
            Integer unitPrice,
            Integer quantity,
            Integer lineAmount
    ) {}
}
