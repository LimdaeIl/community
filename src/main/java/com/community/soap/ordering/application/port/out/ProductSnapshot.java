package com.community.soap.ordering.application.port.out;

import com.community.soap.catalog.domain.entity.ProductStatus;

public record ProductSnapshot(
        Long productId,
        String name,
        String skuCode,
        Integer unitPrice,
        ProductStatus status,
        boolean isDeleted
) { }