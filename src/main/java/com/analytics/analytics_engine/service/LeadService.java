package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.LeadRequestDTO;
import com.analytics.analytics_engine.dto.LeadResponseDTO;

public interface LeadService {
    LeadResponseDTO createLead(LeadRequestDTO lead);
}
