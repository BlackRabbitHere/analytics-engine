package com.analytics.analytics_engine.service;

import com.analytics.analytics_engine.dto.ProductRequestDTO;
import com.analytics.analytics_engine.dto.ProductResponseDTO;
import com.analytics.analytics_engine.exception.APIException;
import com.analytics.analytics_engine.exception.ErrorCode;
import com.analytics.analytics_engine.model.Product;
import com.analytics.analytics_engine.repository.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;


@Service
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {


        String sku=productRequestDTO.getSku().trim();
        String description=productRequestDTO.getDescription();
        String name=productRequestDTO.getName();
        BigDecimal price=productRequestDTO.getPrice();
        String currency=productRequestDTO.getCurrency();

        Optional<Product> product=productRepository.getProductBySku(sku);
        if(product.isPresent()){
            throw new APIException(ErrorCode.PRODUCT_DUPLICATE_SKU);
        }

        Product newProduct=new Product(sku,name,description,price,currency);
        Product savedProduct;
        try {
        savedProduct=productRepository.saveAndFlush(newProduct);
        }catch (DataIntegrityViolationException ex){
            throw new APIException(ErrorCode.PRODUCT_CONFLICT,ex);
        }


        return ProductResponseDTO.toResponse(savedProduct);
    }
}
