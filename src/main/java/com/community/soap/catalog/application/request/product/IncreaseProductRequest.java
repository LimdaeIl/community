package com.community.soap.catalog.application.request.product;

import jakarta.validation.constraints.PositiveOrZero;

public record IncreaseProductRequest(
        @PositiveOrZero Integer quantity
) {

}
