package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class UserActivityServiceImpl implements UserActivityService{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void recordServerEvent(
            UserActivity.EventType eventType,
            User user,
            Lead lead,
            Order order,
            Payment payment,
            Instant occurredAt
    ) {
        UserActivity activity = new UserActivity(
                UUID.randomUUID(),           // Unique event ID
                user,
                eventType,
                UserActivity.Source.SERVER,
                "analytics-engine",          // Example application ID
                null,                        // Session ID is optional
                occurredAt,
                null,                        // No single product reference
                order,
                payment,
                lead,
                Map.of()                     // Optional extra properties
        );

        entityManager.persist(activity);
    }

}
