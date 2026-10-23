package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.LeadRequestDTO;
import com.analytics.analytics_engine.dto.LeadResponseDTO;
import com.analytics.analytics_engine.exception.APIException;
import com.analytics.analytics_engine.exception.ErrorCode;
import com.analytics.analytics_engine.model.Lead;
import com.analytics.analytics_engine.model.User;
import com.analytics.analytics_engine.repository.LeadRepository;
import com.analytics.analytics_engine.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LeadServiceImpl implements LeadService{
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    public LeadServiceImpl(UserRepository userRepository, LeadRepository leadRepository) {
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
    }


    @Override
    public LeadResponseDTO createLead(LeadRequestDTO lead) {
        long userId=lead.getUserId();
        String source=lead.getSource()
                .strip()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s-_]+", "_") //convert the insider spaces , _ to _
                .replaceAll("^_|_$", ""); // convert the trailing and leading __ to blank
        if(source.isBlank() || source.length() > 100){
            throw new APIException(ErrorCode.VALIDATION_FAILED);
        }
        User user=userRepository.getUserById(userId).orElseThrow(
                ()-> new APIException(ErrorCode.USER_NOT_FOUND));

        Lead newLead=new Lead(user,source);
        newLead.setStatus(Lead.Status.NEW);
        leadRepository.saveAndFlush(newLead);

        return LeadResponseDTO.toResponse(newLead);
    }
}
