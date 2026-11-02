package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.model.*;

import java.time.Instant;

public interface UserActivityService {

    void recordServerEvent(
            UserActivity.EventType eventType,
            User user,
            Lead lead,
            Order order,
            Payment payment,
            Instant occurredAt
    );

}
