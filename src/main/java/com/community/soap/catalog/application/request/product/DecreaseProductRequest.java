package com.community.soap.catalog.application.request.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DecreaseProductRequest(
        @NotNull @Positive Integer quantity
) {

}
