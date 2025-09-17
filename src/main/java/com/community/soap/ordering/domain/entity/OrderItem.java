package com.community.soap.ordering.domain.entity;

import com.community.soap.ordering.domain.exception.OrderErrorCode;
import com.community.soap.ordering.domain.exception.OrderException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "c_order_item")
@Entity
public class OrderItem {

    @Id
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sku_code", nullable = false)
    private String skuCode;

    @Column(name = "product_name", nullable = false)
    private String productName; // 주문 시점 스냅샷

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;  // 원 단위

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_amount", nullable = false)
    private Integer lineAmount; // unitPrice * quantity

    private OrderItem(
            Long orderItemId,
            Long productId,
            String skuCode,
            String productName,
            Integer unitPrice,
            int quantity
    ) {
        if (orderItemId == null) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_INVALID);
        }
        if (productId == null) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_INVALID);
        }
        if (skuCode == null || skuCode.isBlank()) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_INVALID);
        }
        if (productName == null || productName.isBlank()) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_INVALID);
        }
        if (unitPrice == null || unitPrice < 0) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_INVALID);
        }
        if (quantity <= 0) {
            throw new OrderException(OrderErrorCode.QUANTITY_INVALID);
        }

        this.orderItemId = orderItemId;
        this.productId = productId;
        this.skuCode = skuCode;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        recalc();
    }

    public static OrderItem of(
            Long orderItemId,
            Long productId,
            String skuCode,
            String productName,
            Integer unitPrice,
            int quantity
    ) {
        return new OrderItem(orderItemId, productId, skuCode, productName, unitPrice, quantity);
    }

    /* 내부 유틸 */
    void attach(Order order) {
        if (order == null) {
            throw new OrderException(OrderErrorCode.ORDER_INVALID);
        }
        this.order = order;
    }

    private void recalc() {
        this.lineAmount = this.unitPrice * this.quantity;
    }
}
