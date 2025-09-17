package com.community.soap.catalog.application.request.product;

import org.springframework.util.StringUtils;

public record ProductSearchCondition(
        Long categoryId,
        Integer minPrice,
        Integer maxPrice,
        String keyword,   // 상품명 검색(부분)
        String sort,      // createdAt | name | price   (컬럼 매핑은 서비스에서)
        String order,     // asc | desc
        Integer page,     // 0-base
        Integer size      // page size
) {

    public boolean hasKeyword() {
        return StringUtils.hasText(keyword);
    }

    public String normalizedSort() {
        return switch (sort == null ? "" : sort) {
            case "name", "price", "createdAt" -> sort;
            default -> "createdAt";
        };
    }

    public String normalizedOrder() {
        return "asc".equalsIgnoreCase(order) ? "asc" : "desc";
    }
}
