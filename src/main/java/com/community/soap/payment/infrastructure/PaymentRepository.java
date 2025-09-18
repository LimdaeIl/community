package com.community.soap.payment.infrastructure;

import com.community.soap.payment.domain.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);
}
