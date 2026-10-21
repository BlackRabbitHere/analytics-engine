package com.analytics.analytics_engine.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {

    public enum Status {
        SUCCESS,
        REFUNDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "order_id",
        nullable = false,
        unique = true,
        updatable = false
    )
    private Order order;

    @Column(
        name = "provider_reference",
        nullable = false,
        unique = true,
        updatable = false,
        length = 100
    )
    private String providerReference;

    @Column(
        name = "amount",
        nullable = false,
        updatable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.SUCCESS;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    public Payment() {
    }

    // Generate getters and setters.
}