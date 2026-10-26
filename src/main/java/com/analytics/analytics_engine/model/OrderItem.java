package com.analytics.analytics_engine.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(
        name = "order_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_items_order_product",
                columnNames = {"order_id", "product_id"}
        )
)
@Getter
public class OrderItem {

    private static final BigDecimal MAX_AMOUNT =
            new BigDecimal("9999999999.99");

    protected OrderItem() {
    }

    public OrderItem(Order order, Product product, int quantity) {
        this.order = Objects.requireNonNull(order, "Order is required");
        this.product = Objects.requireNonNull(
                product, "Product is required"
        );

        if (order.getStatus() != Order.Status.PENDING) {
            throw new IllegalArgumentException(
                    "Items can only be added to a pending order"
            );
        }

        if (!product.isActive()) {
            throw new IllegalArgumentException(
                    "Inactive products cannot be ordered"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        this.quantity = quantity;

        // Purchase snapshots: never read historical prices
        // or names from the current product catalog.
        this.productSku = product.getSku();
        this.productName = product.getName();
        this.currency = product.getCurrency();

        this.unitPrice = product.getPrice()
                .setScale(2, RoundingMode.UNNECESSARY);

        this.lineTotal = unitPrice.multiply(
                BigDecimal.valueOf(quantity)
        );

        if (lineTotal.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException(
                    "Order item total is too large"
            );
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            updatable = false
    )
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            updatable = false
    )
    private Product product;

    @Column(
            name = "product_sku",
            nullable = false,
            updatable = false,
            length = 64
    )
    private String productSku;

    @Column(
            name = "product_name",
            nullable = false,
            updatable = false,
            length = 200
    )
    private String productName;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Column(
            name = "unit_price",
            nullable = false,
            updatable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(
            name = "line_total",
            nullable = false,
            updatable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal lineTotal;

    @Column(nullable = false, updatable = false, length = 3)
    private String currency;
}