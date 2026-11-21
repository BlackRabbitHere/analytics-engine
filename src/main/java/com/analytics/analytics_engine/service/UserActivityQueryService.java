package com.analytics.analytics_engine.service;

import com.analytics.analytics_engine.dto.UserActivitySummaryDTO;
import com.analytics.analytics_engine.exception.APIException;
import com.analytics.analytics_engine.exception.ErrorCode;
import com.analytics.analytics_engine.model.UserActivity.EventType;
import com.analytics.analytics_engine.repository.UserActivityRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.Map;

@Service
public class UserActivityQueryService {

    private final UserActivityRepository repository;

    public UserActivityQueryService(UserActivityRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "activity-summary",
            key = "#p0.toString() + ':' + #p1.toString() + ':' + #p2.toString()"
    )
    public UserActivitySummaryDTO getSummary(long userId, LocalDate from, LocalDate to) {
        if (userId <= 0 || !from.isBefore(to)) {
            throw new APIException(ErrorCode.VALIDATION_FAILED);
        }

        Instant fromTime = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toTime = to.atStartOfDay(ZoneOffset.UTC).toInstant();

        Map<EventType, Long> counts = new EnumMap<>(EventType.class);
        for (var row : repository.countEventsByType(userId, fromTime, toTime)) {
            counts.put(row.getEventType(), row.getEventCount());
        }

        return new UserActivitySummaryDTO(userId, from, to, counts);
    }
}