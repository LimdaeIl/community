package com.community.soap.catalog.application.request.product;

import com.community.soap.catalog.domain.entity.ProductStatus;

public record UpdateProductRequest(
        String name,
        String description,
        Long categoryId,
        ProductStatus productStatus,
        String skuCode,
        Integer price,
        Integer stock
) {

}
