package com.analytics.analytics_engine.controller;

import com.analytics.analytics_engine.dto.UserActivitySummaryDTO;
import com.analytics.analytics_engine.service.UserActivityQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class AnalyticsController {

    private final UserActivityQueryService service;

    public AnalyticsController(UserActivityQueryService service) {
        this.service = service;
    }

    @GetMapping("/api/analytics/users/{userId}/activity-summary")
    public UserActivitySummaryDTO getSummary(
            @PathVariable("userId") long userId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.getSummary(userId, from, to);
    }
}