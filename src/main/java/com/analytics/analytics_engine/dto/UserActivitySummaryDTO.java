package com.analytics.analytics_engine.dto;

import com.analytics.analytics_engine.model.UserActivity.EventType;

import java.time.LocalDate;
import java.util.Map;

public record UserActivitySummaryDTO(
        long userId,
        LocalDate from,
        LocalDate to,
        Map<EventType, Long> counts
) {}