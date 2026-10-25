package com.analytics.analytics_engine.repository;

import com.analytics.analytics_engine.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

}
