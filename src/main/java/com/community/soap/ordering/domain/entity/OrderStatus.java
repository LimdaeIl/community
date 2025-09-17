package com.community.soap.ordering.domain.entity;

public enum OrderStatus {
    CREATED,   // 생성(장바구니 확정 직후)
    PAID,      // 결제완료
    SHIPPED,   // 출고/배송중
    DELIVERED, // 배송완료
    CANCELED   // 취소
}
