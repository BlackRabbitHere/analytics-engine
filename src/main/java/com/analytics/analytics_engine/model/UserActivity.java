package com.analytics.analytics_engine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_activity")
@Getter
public class UserActivity {

    protected UserActivity() {
    }


    public UserActivity(
            UUID eventId,
            User user,
            String eventType,
            Instant occurredAt
    ) {
        this.eventId = Objects.requireNonNull(
                eventId, "Event ID is required"
        ).toString();

        this.user = Objects.requireNonNull(
                user, "User is required"
        );

        this.occurredAt = Objects.requireNonNull(
                occurredAt, "Occurrence time is required"
        );

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Event type is required");
        }

        if (eventType.length() > 100) {
            throw new IllegalArgumentException(
                    "Event type must not exceed 100 characters"
            );
        }

        this.eventType = eventType;
    }


    @Id
    @Column(
        name = "event_id",
        nullable = false,
        updatable = false,
        length = 36
    )
    private String eventId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;


    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;



}