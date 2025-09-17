package com.community.soap.catalog.application.request.product;

import jakarta.validation.constraints.PositiveOrZero;

public record DecreaseProductRequest(
        @PositiveOrZero Integer quantity
) {

}
