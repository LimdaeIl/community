package com.community.soap.ordering.application.port.out;

import com.community.soap.ordering.application.request.OrderSearchCondition;
import com.community.soap.ordering.domain.entity.Order;
import java.util.Optional;
<<<<<<< Updated upstream
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
=======
>>>>>>> Stashed changes

public interface OrderRepositoryPort {

    Order save(Order order);

<<<<<<< Updated upstream
    Optional<Order> findById(Long orderId);
    Page<Order> findAllByCondition(OrderSearchCondition condition, Pageable pageable);
=======
    Optional<Order> findById(Long id);

>>>>>>> Stashed changes
}
