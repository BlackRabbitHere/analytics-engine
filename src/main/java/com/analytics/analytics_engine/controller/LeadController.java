package com.analytics.analytics_engine.controller;


import com.analytics.analytics_engine.dto.LeadRequestDTO;
import com.analytics.analytics_engine.dto.LeadResponseDTO;
import com.analytics.analytics_engine.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping("/create-lead")
    public ResponseEntity<LeadResponseDTO> createLead(@Valid @RequestBody LeadRequestDTO lead){
        LeadResponseDTO newLead=leadService.createLead(lead);
        return new ResponseEntity<>(newLead,HttpStatus.CREATED);
    }


}
