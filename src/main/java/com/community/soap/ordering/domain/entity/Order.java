package com.community.soap.ordering.domain.entity;

import com.community.soap.ordering.domain.exception.OrderErrorCode;
import com.community.soap.ordering.domain.exception.OrderException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "c_orders")
@Entity
public class Order {

    @Id
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 40)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> orderItems = new ArrayList<>();

    @Embedded
    private Address address;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    private Order(Long orderId, Address address, Long createdBy) {
        if (orderId == null) {
            throw new OrderException(OrderErrorCode.ORDER_INVALID);
        }
        if (address == null) {
            throw new OrderException(OrderErrorCode.ADDRESS_INVALID);
        }
        if (createdBy == null) {
            throw new OrderException(OrderErrorCode.ORDER_INVALID);
        }

        this.orderId = orderId;
        this.orderStatus = OrderStatus.CREATED; // 초기 상태
        this.address = address;
        this.totalAmount = 0;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
        this.updatedAt = null;
        this.updatedBy = null;
    }

    public static Order of(Long orderId, Address address, Long createdBy) {
        return new Order(orderId, address, createdBy);
    }

    /* 최소 도메인 동작: 아이템 추가 + 합계 재계산 */
    public void addItem(OrderItem item) {
        if (item == null) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_INVALID);
        }
        item.attach(this);
        this.orderItems.add(item);
        recalcTotals();
    }

    private void recalcTotals() {
        int sum = 0;
        for (OrderItem it : orderItems) {
            sum += it.getLineAmount();
        }
        this.totalAmount = sum;
    }
    public void softDelete(Long userId) {
        this.isDeleted = true;
        update(userId);
    }

    private void update(Long userId) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void markPaid() {
        if (this.orderStatus == OrderStatus.CANCELED) {
            throw new OrderException(OrderErrorCode.ORDER_ALREADY_CANCELED);
        }
        if (this.orderStatus == OrderStatus.PAID) {
            throw new OrderException(OrderErrorCode.ORDER_ALREADY_PAID);
        }
        this.orderStatus = OrderStatus.PAID;
        this.updatedAt = LocalDateTime.now();
        // this.updatedBy = ... 필요하면 채워넣기
    }

    // (참고) 취소 등 다른 전이도 필요하면 같은 패턴으로
    public void markCanceled() {
        if (this.orderStatus == OrderStatus.CANCELED) {
            throw new OrderException(OrderErrorCode.ORDER_ALREADY_CANCELED);
        }
        if (this.orderStatus == OrderStatus.SHIPPED || this.orderStatus == OrderStatus.DELIVERED) {
            throw new OrderException(OrderErrorCode.ORDER_CANNOT_CANCEL);
        }
        this.orderStatus = OrderStatus.CANCELED;
        this.updatedAt = LocalDateTime.now();
    }
}

