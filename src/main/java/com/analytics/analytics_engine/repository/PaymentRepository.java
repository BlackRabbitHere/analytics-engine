package com.analytics.analytics_engine.repository;

import com.analytics.analytics_engine.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    Optional<Payment> findByOrder_Id(Long orderId);

    Optional<Payment> findByProviderReference(String reference);
}
