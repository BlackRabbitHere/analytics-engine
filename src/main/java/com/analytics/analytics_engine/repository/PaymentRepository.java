package com.analytics.analytics_engine.repository;

import com.analytics.analytics_engine.model.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    Optional<Payment> findByOrder_Id(Long orderId);

    Optional<Payment> findByProviderReference(String reference);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.providerReference=:providerReference")
    Optional<Payment> findByProviderReferenceForUpdate(@Param("providerReference")String providerReference);
}
