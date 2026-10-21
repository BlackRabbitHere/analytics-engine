package com.analytics.analytics_engine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
public class Payment {

    private static final BigDecimal MAX_ALLOWED = new BigDecimal("9999999999.99");
    private static final int MAX_SCALE = 2;

    public Payment() {
    }

    public Payment(Order order,String providerReference,BigDecimal amount){
        if(order==null){
            throw new IllegalArgumentException("Order is Required");
        }

        if(providerReference==null || providerReference.isBlank()){
            throw new IllegalArgumentException("Provider reference is Required");
        }

        if(providerReference.length()>100){
            throw new IllegalArgumentException("Provider length cannot exceed 100");
        }

        if(amount==null){
            throw new IllegalArgumentException("Amount is Required");
        }



        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        if(amount.compareTo(MAX_ALLOWED)>0){
            throw new IllegalArgumentException("Amount is too large");
        }

        if(amount.stripTrailingZeros().scale()>MAX_SCALE){
            throw new IllegalArgumentException("Amount should not exceed "+MAX_SCALE+" decimal places");
        }

        if(!order.getAmount().equals(amount)){
            throw new IllegalArgumentException("Payment amount and Order amount mismatch");
        }

        this.order=order;
        this.providerReference=providerReference;
        this.amount=amount;
    }

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

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.SUCCESS;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Setter
    @Column(name = "refunded_at")
    private Instant refundedAt;


}