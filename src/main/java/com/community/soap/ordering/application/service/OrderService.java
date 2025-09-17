package com.community.soap.ordering.application.service;

import com.community.soap.catalog.domain.entity.ProductStatus;
import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.ordering.application.port.in.OrderUseCase;
import com.community.soap.ordering.application.port.out.CatalogPort;
import com.community.soap.ordering.application.port.out.OrderRepositoryPort;
import com.community.soap.ordering.application.port.out.ProductSnapshot;
import com.community.soap.ordering.application.request.CreateOrderItemRequest;
import com.community.soap.ordering.application.request.CreateOrderRequest;
import com.community.soap.ordering.application.response.CreateOrderResponse;
import com.community.soap.ordering.domain.entity.Address;
import com.community.soap.ordering.domain.entity.Order;
import com.community.soap.ordering.domain.entity.OrderItem;
import com.community.soap.ordering.domain.exception.OrderErrorCode;
import com.community.soap.ordering.domain.exception.OrderException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService implements OrderUseCase {

    private final Snowflake snowflake;
    private final OrderRepositoryPort orderRepositoryPort;
    private final CatalogPort catalogPort;

    @Transactional
    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        // 1) 주문 생성
        Address addr = Address.of(
                request.address().receiverName(),
                request.address().phone(),
                request.address().zipcode(),
                request.address().address1(),
                request.address().address2()
        );
        Order order = Order.of(snowflake.nextId(), addr, 0L);

        // 2) 각 아이템 처리
        for (CreateOrderItemRequest item : request.items()) {
            ProductSnapshot snap = catalogPort.getProductSnapshot(item.productId());

            // 판매 가능성 체크
            if (snap.isDeleted() || snap.status() == ProductStatus.INACTIVE) {
                throw new OrderException(OrderErrorCode.PRODUCT_NOT_SALEABLE);
            }

            if (!snap.skuCode().equals(item.skuCode())) {
                throw new OrderException(OrderErrorCode.SKU_MISMATCH);
            }

            // V1: 비원자적 재고 감소 시도
            boolean ok = catalogPort.tryReserveStock(item.productId(), item.quantity());
            if (!ok) {
                throw new OrderException(OrderErrorCode.STOCK_INSUFFICIENT);
            }

            // 라인 추가(스냅샷 기준)
            OrderItem line = OrderItem.of(
                    snowflake.nextId(),
                    item.productId(),
                    snap.skuCode(),
                    snap.name(),
                    snap.unitPrice(),
                    item.quantity()
            );
            order.addItem(line);
        }

        // 3) 저장
        orderRepositoryPort.save(order);

        // 4) 응답
        var response = order.getOrderItems().stream()
                .map(li -> new CreateOrderResponse.Line(
                        li.getOrderItemId(), li.getProductId(), li.getSkuCode(),
                        li.getProductName(), li.getUnitPrice(), li.getQuantity(),
                        li.getLineAmount()))
                .collect(Collectors.toList());

        return new CreateOrderResponse(order.getOrderId(), order.getOrderStatus(),
                order.getTotalAmount(), response);
    }
}
