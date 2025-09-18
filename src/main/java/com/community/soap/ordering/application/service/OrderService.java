package com.community.soap.ordering.application.service;

import com.community.soap.catalog.domain.entity.ProductStatus;
import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.common.util.PageResponse;
import com.community.soap.ordering.application.port.in.OrderUseCase;
import com.community.soap.ordering.application.port.out.CatalogPort;
import com.community.soap.ordering.application.port.out.OrderRepositoryPort;
import com.community.soap.ordering.application.port.out.ProductSnapshot;
import com.community.soap.ordering.application.request.CreateOrderItemRequest;
import com.community.soap.ordering.application.request.CreateOrderRequest;
import com.community.soap.ordering.application.request.OrderSearchCondition;
import com.community.soap.ordering.application.response.GetOrderResponse;
import com.community.soap.ordering.domain.entity.Address;
import com.community.soap.ordering.domain.entity.Order;
import com.community.soap.ordering.domain.entity.OrderItem;
import com.community.soap.ordering.domain.exception.OrderErrorCode;
import com.community.soap.ordering.domain.exception.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService implements OrderUseCase {

    private final Snowflake snowflake;
    private final OrderRepositoryPort orderRepositoryPort;
    private final CatalogPort catalogPort;

    private Order findOrderByOrderId(Long orderId) {
        return orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional
    @Override
    public GetOrderResponse createOrder(CreateOrderRequest request) {
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

        return GetOrderResponse.from(order);
    }

    @Transactional
    @Override
    public GetOrderResponse getOrder(Long orderId) {
        Order orderById = findOrderByOrderId(orderId);

        return GetOrderResponse.from(orderById);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<GetOrderResponse> getOrders(OrderSearchCondition c) {
        int page = (c.page() == null || c.page() < 0) ? 0 : c.page();
        int size = (c.size() == null || c.size() <= 0) ? 20 : Math.min(c.size(), 200);

        if (c.minTotal() != null && c.maxTotal() != null && c.maxTotal() < c.minTotal()) {
            throw new OrderException(OrderErrorCode.INVALID_AMOUNT_RANGE);
        }
        if (c.from() != null && c.to() != null && c.to().isBefore(c.from())) {
            throw new OrderException(OrderErrorCode.INVALID_AMOUNT_RANGE);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<GetOrderResponse> result = orderRepositoryPort
                .findAllByCondition(c, pageable)
                .map(GetOrderResponse::from);

        return PageResponse.from(result);
    }

    @Transactional
    @Override
    public void softDeleteOrder(Long orderId) {
        Order orderById = findOrderByOrderId(orderId);

        orderById.softDelete(0L);
    }


}
