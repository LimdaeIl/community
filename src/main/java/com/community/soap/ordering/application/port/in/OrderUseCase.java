package com.community.soap.ordering.application.port.in;

import com.community.soap.common.util.PageResponse;
import com.community.soap.ordering.application.request.CreateOrderRequest;
import com.community.soap.ordering.application.request.OrderSearchCondition;
import com.community.soap.ordering.application.response.GetOrderResponse;

public interface OrderUseCase {

    GetOrderResponse createOrder(CreateOrderRequest request);

    GetOrderResponse getOrder(Long orderId);

    PageResponse<GetOrderResponse> getOrders(OrderSearchCondition c);

    void softDeleteOrder(Long orderId);
}
