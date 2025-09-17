package com.community.soap.ordering.application.port.in;

import com.community.soap.ordering.application.request.CreateOrderRequest;
import com.community.soap.ordering.application.response.CreateOrderResponse;

public interface OrderUseCase {

    CreateOrderResponse createOrder(CreateOrderRequest request);
}
