package com.analytics.analytics_engine.service;

import com.analytics.analytics_engine.dto.ProductRequestDTO;
import com.analytics.analytics_engine.dto.ProductResponseDTO;

public interface ProductService {

    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);
}
