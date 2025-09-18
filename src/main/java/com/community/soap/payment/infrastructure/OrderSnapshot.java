package com.community.soap.payment.infrastructure;

import com.community.soap.ordering.domain.entity.OrderStatus;

public record OrderSnapshot(
        Long orderId,
        Integer totalAmount,
        OrderStatus status
) { }