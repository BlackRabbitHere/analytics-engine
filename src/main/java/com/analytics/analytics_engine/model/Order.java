package com.analytics.analytics_engine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
public class Order {

    public Order() {
    }

    public Order(User user, Lead lead,BigDecimal amount){
        if(user==null){
            throw new IllegalArgumentException("User is required");
        }

        if(lead==null){
            throw new IllegalArgumentException("Lead is required");
        }

        if(amount==null){
            throw new IllegalArgumentException("Amount is required");
        }

        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        this.user=user;
        this.lead=lead;
        this.amount=amount;
    }

    public enum Status {
        PENDING,
        PAID,
        REFUNDED
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;


    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Setter
    @Column(name = "paid_at")
    private Instant paidAt;

    @Setter
    @Column(name = "refunded_at")
    private Instant refundedAt;



}