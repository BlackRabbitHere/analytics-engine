package com.analytics.analytics_engine.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponseDTO {

    public ApiResponseDTO(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    protected ApiResponseDTO(){

    }
    public String message;

    private boolean status;
}
