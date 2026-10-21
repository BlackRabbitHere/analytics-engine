package com.analytics.analytics_engine.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "orders")
@Getter
public class Order {
    private static final BigDecimal MAX_ALLOWED = new BigDecimal("9999999999.99");
    private static final int MAX_SCALE = 2;

    public Order() {
    }

    public Order(User user, Lead lead, BigDecimal amount){
        if(user==null){
            throw new IllegalArgumentException("User is required");
        }

        if(lead==null){
            throw new IllegalArgumentException("Lead is required");
        }

        User leadUser=lead.getUser();
        if(leadUser==null || user.getId()==null || !user.getId().equals(leadUser.getId())){
            throw new IllegalArgumentException("Lead does not belong to user");
        }

        if(amount==null){
            throw new IllegalArgumentException("Amount is required");
        }

        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if(amount.compareTo(MAX_ALLOWED)>0){
            throw new IllegalArgumentException("Amount is too large");
        }

        if(amount.stripTrailingZeros().scale()>MAX_SCALE){
            throw new IllegalArgumentException("Amount should not exceed "+MAX_SCALE+" decimal places");
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @Column(name = "paid_at")
    private Instant paidAt;


    @Column(name = "refunded_at")
    private Instant refundedAt;


    public void markPaid(Instant paymentTime){
        Objects.requireNonNull(paymentTime,"Payment time is required");

//        Preserve original timestamp
        if(status==Status.PAID){
            return;
        }

        if(status!=Status.PENDING){
            throw new IllegalStateException("Only a pending order can be marked as paid");
        }


        this.status=Status.PAID;
        this.paidAt=paymentTime;
    }

    public void refund(Instant refundedTime){
        Objects.requireNonNull(refundedTime,"Refund time is required");

//        Preserve the earlier refund timestamp
        if(status==Status.REFUNDED){
            return;
        }

        if(status!=Status.PAID){
            throw new IllegalStateException("Only a paid order can be refunded");

        }

        this.status=Status.REFUNDED;
        this.refundedAt=refundedTime;

    }


}