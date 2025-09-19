package com.community.soap.payment.infrastructure;


import java.util.List;

public interface OrderingPort {
    OrderSnapshot getOrderSnapshot(Long orderId);
    void markOrderPaid(Long orderId);

    // 추가
    void markOrderCanceled(Long orderId);         // 주문 상태를 CANCELED로
    List<OrderLineItem> getOrderLineItems(Long orderId); // 재고 복원을 위해 라인아이템 조회

}
