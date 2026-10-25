package com.analytics.analytics_engine.repository;

import com.analytics.analytics_engine.model.Lead;
import com.analytics.analytics_engine.model.Order;
import com.analytics.analytics_engine.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {


    boolean existsByUserIdAndLeadId(Long userId, Long leadId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id=:orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") Long orderId);
}
