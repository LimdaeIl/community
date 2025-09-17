package com.community.soap.ordering.application.port.out;

import com.community.soap.ordering.domain.entity.Order;

public interface OrderRepositoryPort {

    Order save(Order order);

}
