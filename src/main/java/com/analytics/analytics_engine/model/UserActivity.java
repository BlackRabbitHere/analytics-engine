package com.analytics.analytics_engine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_activity")
@Getter
public class UserActivity {

    @Id
    @Column(
        name = "event_id",
        nullable = false,
        updatable = false,
        length = 36
    )
    private String eventId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Setter
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    public UserActivity() {
    }

    // Generate getters and setters.
}