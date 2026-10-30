package com.analytics.analytics_engine.repository;

import com.analytics.analytics_engine.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    Optional<Product> getProductBySku(String sku);
}
