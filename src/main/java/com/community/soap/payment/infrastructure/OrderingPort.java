package com.community.soap.payment.infrastructure;


public interface OrderingPort {
    OrderSnapshot getOrderSnapshot(Long orderId);
    void markOrderPaid(Long orderId);
}
