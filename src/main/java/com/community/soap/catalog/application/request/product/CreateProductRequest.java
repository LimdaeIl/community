package com.community.soap.catalog.application.request.product;

import com.community.soap.catalog.domain.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateProductRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull Long categoryId,
        @NotNull ProductStatus productStatus,
        @NotBlank String skuCode,
        @NotNull @PositiveOrZero Integer price,
        @NotNull @PositiveOrZero Integer stock
) {

}
