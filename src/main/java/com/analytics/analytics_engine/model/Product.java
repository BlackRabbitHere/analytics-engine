package com.analytics.analytics_engine.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Currency;

@Entity
@Table(
        name = "products",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_products_sku",
                columnNames = "sku"
        )
)
@Getter
public class Product {

    private static final BigDecimal MAX_PRICE =
            new BigDecimal("9999999999.99");

    protected Product() {
    }

    public Product(
            String sku,
            String name,
            String description,
            BigDecimal price,
            String currency
    ) {
        this.sku = requiredText(sku, "SKU", 64);

        String currencyCode = requiredText(currency, "Currency", 3);

        if (!currencyCode.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException(
                    "Currency must be a three-letter uppercase code"
            );
        }

        Currency.getInstance(currencyCode);
        this.currency = currencyCode;

        updateDetails(name, description);
        changePrice(price);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 64)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, updatable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void updateDetails(String name, String description) {
        String validatedName = requiredText(name, "Name", 200);

        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 2000 characters"
            );
        }

        this.name = validatedName;
        this.description = description;
    }

    public void changePrice(BigDecimal price) {
        if (price == null
                || price.signum() <= 0
                || price.compareTo(MAX_PRICE) > 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than zero and fit DECIMAL(12,2)"
            );
        }

        try {
            this.price = price.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Price must have at most two decimal places",
                    exception
            );
        }
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    private static String requiredText(
            String value,
            String field,
            int maximumLength
    ) {
        if (value == null
                || value.isBlank()
                || value.trim().length() > maximumLength) {
            throw new IllegalArgumentException(
                    field + " is required and must not exceed "
                            + maximumLength + " characters"
            );
        }

        return value.trim();
    }
}