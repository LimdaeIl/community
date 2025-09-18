package com.community.soap.ordering.presentation;

import com.community.soap.common.util.PageResponse;
import com.community.soap.ordering.application.port.in.OrderUseCase;
import com.community.soap.ordering.application.request.CreateOrderRequest;
import com.community.soap.ordering.application.request.OrderSearchCondition;
import com.community.soap.ordering.application.response.GetOrderResponse;
import com.community.soap.ordering.domain.entity.OrderStatus;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@RestController
public class OrderController {

    private final OrderUseCase orderUseCase;

    @PostMapping
    public ResponseEntity<GetOrderResponse> orderResponse(
            @RequestBody CreateOrderRequest request
    ) {

        GetOrderResponse response = orderUseCase.createOrder(request);

        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> orderResponse(
            @PathVariable Long orderId
    ) {
        GetOrderResponse response = orderUseCase.getOrder(orderId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<GetOrderResponse>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer minTotal,
            @RequestParam(required = false) Integer maxTotal,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sort,     // createdAt | totalAmount
            @RequestParam(defaultValue = "desc") String order,         // asc | desc
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        OrderSearchCondition cond = new OrderSearchCondition(
                status, createdBy, from, to, minTotal, maxTotal, keyword, sort, order, page, size
        );
        PageResponse<GetOrderResponse> response = orderUseCase.getOrders(cond);
        return ResponseEntity.ok(response);
    }

    // 주문 상태 수정

    // 주문 삭제(Soft Delete)
    @DeleteMapping("/{orderId{")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long orderId
    ) {
        orderUseCase.softDeleteOrder(orderId);
        return ResponseEntity
                .noContent()
                .build();
    }


}
