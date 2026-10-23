package com.analytics.analytics_engine.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO {
    private boolean success;
    private int httpStatus;
    private String errorCode;
    private String message;
    private Map<String, List<String>> fieldErrors;
}
