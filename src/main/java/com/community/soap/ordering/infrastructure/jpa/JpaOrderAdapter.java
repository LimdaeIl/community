package com.community.soap.ordering.infrastructure.jpa;

import com.community.soap.ordering.application.port.out.OrderRepositoryPort;
import com.community.soap.ordering.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderAdapter extends JpaRepository<Order, Long>, OrderRepositoryPort {

}
