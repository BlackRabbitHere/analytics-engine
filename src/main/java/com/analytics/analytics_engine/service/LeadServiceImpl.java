package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.LeadRequestDTO;
import com.analytics.analytics_engine.dto.LeadResponseDTO;
import com.analytics.analytics_engine.exception.APIException;
import com.analytics.analytics_engine.exception.ErrorCode;
import com.analytics.analytics_engine.model.Lead;
import com.analytics.analytics_engine.model.User;
import com.analytics.analytics_engine.model.UserActivity;
import com.analytics.analytics_engine.repository.LeadRepository;
import com.analytics.analytics_engine.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LeadServiceImpl implements LeadService{
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final UserActivityService userActivityService;

    public LeadServiceImpl(UserRepository userRepository, LeadRepository leadRepository, UserActivityService userActivityService) {
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
        this.userActivityService = userActivityService;
    }


    @Override
    @Transactional
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

        userActivityService.recordServerEvent(
                UserActivity.EventType.LEAD_CREATED,
                newLead.getUser(),
                newLead,
                null,
                null,
                newLead.getCreatedAt()
        );


        return LeadResponseDTO.toResponse(newLead);
    }
}
