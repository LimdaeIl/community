package com.community.soap.catalog.application.response.product;

import com.community.soap.catalog.domain.entity.Product;
import com.community.soap.catalog.domain.entity.ProductStatus;
import com.community.soap.catalog.domain.entity.Sku;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record CreateProductResponse(
        Long productId,
        String name,
        String description,
        Long categoryId,
        ProductStatus productStatus,
        Boolean isDeleted,
        LocalDateTime createdAt,
        Long createdBy,
        SkUnit skUnit
) {

    public static CreateProductResponse from(Product product) {
        return CreateProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategoryId())
                .productStatus(product.getProductStatus())
                .isDeleted(product.getIsDeleted())
                .createdAt(product.getCreatedAt())
                .createdBy(product.getCreatedBy())
                .skUnit(SkUnit.from(product.getSku()))
                .build();
    }


    @Builder(access = AccessLevel.PRIVATE)
    protected record SkUnit(
            String skuCode,
            Integer price,
            Integer stock
    ) {

        public static SkUnit from(Sku sku) {
            return SkUnit.builder()
                    .skuCode(sku.getCode())
                    .price(sku.getPrice())
                    .stock(sku.getStock())
                    .build();
        }
    }
}
