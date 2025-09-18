package com.community.soap.ordering.application.port.out;

import com.community.soap.ordering.application.request.OrderSearchCondition;
import com.community.soap.ordering.domain.entity.Order;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(Long orderId);
    Page<Order> findAllByCondition(OrderSearchCondition condition, Pageable pageable);
}
