package com.community.soap.ordering.presentation;

import com.community.soap.ordering.application.port.in.OrderUseCase;
import com.community.soap.ordering.application.request.CreateOrderRequest;
import com.community.soap.ordering.application.response.CreateOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@RestController
public class OrderController {

    private final OrderUseCase orderUseCase;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> orderResponse(
            @RequestBody CreateOrderRequest request
    ) {

        CreateOrderResponse response = orderUseCase.createOrder(request);

        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(response);
    }
}
