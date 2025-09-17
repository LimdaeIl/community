package com.community.soap.ordering.application.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
        @NotEmpty List<CreateOrderItemRequest> items,
        @Valid AddressRequest address
) {

}
