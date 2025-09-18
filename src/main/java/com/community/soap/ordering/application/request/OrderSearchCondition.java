package com.community.soap.ordering.application.request;

import com.community.soap.ordering.domain.entity.OrderStatus;
import java.time.LocalDateTime;

public record OrderSearchCondition(
        OrderStatus status,          // 상태 필터 (CREATED/PAID/…)
        Long createdBy,              // 주문자 ID
        LocalDateTime from,          // 생성 시작일(이상)
        LocalDateTime to,            // 생성 종료일(보다 작게, 필요시 이상으로 바꿔도 OK)
        Integer minTotal,            // 최소 합계
        Integer maxTotal,            // 최대 합계
        String keyword,              // 상품명/SKU 키워드 (order_item에서 검색)
        String sort,                 // createdAt | totalAmount
        String order,                // asc | desc
        Integer page,                // 0-base
        Integer size                 // 페이지 크기 (최대 200)
) {
    public String normalizedSort() {
        String s = (sort == null) ? "createdAt" : sort;
        return switch (s) {
            case "totalAmount" -> "total_amount";
            case "createdAt"  -> "created_at";
            default -> "created_at";
        };
    }
    public String normalizedOrder() {
        return "asc".equalsIgnoreCase(order) ? "asc" : "desc";
    }
}
