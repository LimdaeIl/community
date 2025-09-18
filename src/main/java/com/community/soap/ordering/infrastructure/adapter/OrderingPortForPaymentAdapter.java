package com.community.soap.ordering.infrastructure.adapter;

import com.community.soap.ordering.application.port.out.OrderRepositoryPort;
import com.community.soap.ordering.domain.entity.Order;
import com.community.soap.payment.domain.exception.PaymentErrorCode;
import com.community.soap.payment.domain.exception.PaymentException;
import com.community.soap.payment.infrastructure.OrderSnapshot;
import com.community.soap.payment.infrastructure.OrderingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderingPortForPaymentAdapter implements OrderingPort {

    private final OrderRepositoryPort orderRepositoryPort;

    @Override
    public OrderSnapshot getOrderSnapshot(Long orderId) {
        Order o = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND));
        return new OrderSnapshot(o.getOrderId(), o.getTotalAmount(), o.getOrderStatus());
    }

    @Override
    public void markOrderPaid(Long orderId) {
        Order o = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND));
        o.markPaid(); // 도메인 전이
        // 영속 컨텍스트면 flush 시 반영, 별도 save 불필요
    }
}