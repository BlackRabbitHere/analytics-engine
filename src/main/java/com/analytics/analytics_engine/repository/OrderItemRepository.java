package com.analytics.analytics_engine.repository;

import com.analytics.analytics_engine.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_IdOrderByIdAsc(Long orderId);
}