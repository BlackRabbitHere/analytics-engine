package com.analytics.analytics_engine.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "user_activity",
        indexes = {
                @Index(
                        name = "idx_activity_user_time",
                        columnList = "user_id, occurred_at"
                ),
                @Index(
                        name = "idx_activity_type_time",
                        columnList = "event_type, occurred_at"
                ),
                @Index(
                        name = "idx_activity_product_time",
                        columnList = "product_id, occurred_at"
                )
        }
)
@Getter
public class UserActivity {

    public enum EventType {
        PRODUCT_VIEWED,
        CHECKOUT_STARTED,
        LEAD_CREATED,
        ORDER_CREATED,
        PAYMENT_SUCCEEDED,
        PAYMENT_REFUNDED
    }

    public enum Source {
        CLIENT,
        SERVER
    }

    protected UserActivity() {
    }

    public UserActivity(
            UUID eventId,
            User user,
            EventType eventType,
            Source source,
            String appId,
            String sessionId,
            Instant occurredAt,
            Product product,
            Order order,
            Payment payment,
            Lead lead,
            Map<String, String> properties
    ) {
        this.eventId = Objects.requireNonNull(
                eventId, "Event ID is required"
        ).toString();

        this.user = Objects.requireNonNull(
                user, "User is required"
        );

        this.eventType = Objects.requireNonNull(
                eventType, "Event type is required"
        );

        this.source = Objects.requireNonNull(
                source, "Source is required"
        );

        this.occurredAt = Objects.requireNonNull(
                occurredAt, "Occurrence time is required"
        );

        if (appId == null
                || appId.isBlank()
                || appId.trim().length() > 64) {
            throw new IllegalArgumentException(
                    "App ID is required and cannot exceed 64 characters"
            );
        }

        if (sessionId != null
                && (sessionId.isBlank() || sessionId.length() > 100)) {
            throw new IllegalArgumentException(
                    "Session ID must be nonblank and at most 100 characters"
            );
        }

        if (source == Source.CLIENT
                && eventType != EventType.PRODUCT_VIEWED
                && eventType != EventType.CHECKOUT_STARTED) {
            throw new IllegalArgumentException(
                    "This event type can only be recorded by the server"
            );
        }

        this.appId = appId.trim();
        this.sessionId = sessionId;
        this.product = product;
        this.order = order;
        this.payment = payment;
        this.lead = lead;

        this.properties = properties == null
                ? new HashMap<>()
                : new HashMap<>(Map.copyOf(properties));
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
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false,
            updatable = false,
            length = 100
    )
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private Source source;

    @Column(
            name = "app_id",
            nullable = false,
            updatable = false,
            length = 64
    )
    private String appId;

    @Column(name = "session_id", updatable = false, length = 100)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", updatable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", updatable = false)
    private Lead lead;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "properties",
            nullable = false,
            updatable = false,
            columnDefinition = "json"
    )
    private Map<String, String> properties = new HashMap<>();

    @Column(
            name = "schema_version",
            nullable = false,
            updatable = false
    )
    private int schemaVersion = 1;

    @Column(
            name = "occurred_at",
            nullable = false,
            updatable = false
    )
    private Instant occurredAt;

    @CreationTimestamp
    @Column(
            name = "received_at",
            nullable = false,
            updatable = false
    )
    private Instant receivedAt;

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}