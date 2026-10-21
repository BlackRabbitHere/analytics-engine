package com.analytics.analytics_engine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@Getter
@Entity
@Table(name = "leads")
public class Lead {
    public Lead() {
    }

    public Lead(User user,String source){
        if(user==null){
            throw new IllegalArgumentException("User is required");

        }

        if(source==null || source.isBlank()){
            throw new IllegalArgumentException("source is required");
        }

        if(source.length()>100){
            throw new IllegalArgumentException("Source should not exceed 100 characters");
        }

        this.user=user;
        this.source=source;
    }


    public enum Status {
        NEW,
        CONVERTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "source", nullable = false, length = 100)
    private String source;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.NEW;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;



}