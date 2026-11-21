package com.analytics.analytics_engine.repository;

import com.analytics.analytics_engine.model.UserActivity;
import com.analytics.analytics_engine.model.UserActivity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, String> {

    interface EventCount {
        EventType getEventType();
        Long getEventCount();
    }

    @Query("""
        select a.eventType as eventType, count(a) as eventCount
        from UserActivity a
        where a.user.id = :userId
          and a.occurredAt >= :fromTime
          and a.occurredAt < :toTime
        group by a.eventType
        """)
    List<EventCount> countEventsByType(
            @Param("userId") long userId,
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime);
}