package com.analytics.analytics_engine.dto;

import com.analytics.analytics_engine.model.Lead;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeadResponseDTO {
    private Long leadId;
    private Long userId;
    private String source;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static LeadResponseDTO toResponse(Lead lead){
        return new LeadResponseDTO(lead.getId(),
                lead.getUser().getId(),
                lead.getSource(),
                lead.getStatus().toString(),
                lead.getCreatedAt(),
                lead.getUpdatedAt());
    }
}
