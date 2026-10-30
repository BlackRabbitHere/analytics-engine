package com.analytics.analytics_engine.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDTO {

    @NotNull(message = "Sku cannot be null")
    @NotBlank(message = "Sku cannot be blank")
    @Size(max = 64,message = "sku cannot exceed 64 characters")
    String sku;

    @NotNull(message = "Name cannot be null")
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 200,message = "Name cannot exceed 200 characters")
    String name;


    @Size(max = 2000,message = "Description cannot exceed 2000 characters")
    String description;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price should be greater than 0")
    @Digits(integer = 10,fraction = 2,message = "Price cannot exceed 9999999999.99 and should be upto 2 decimal places")
    BigDecimal price;

    @NotNull(message = "Currency cannot be null")
    @NotBlank(message = "currency cannot be blank")
    @Size(max = 3,message = "Currency cannot exceed 3 chars")
    String currency;

}
