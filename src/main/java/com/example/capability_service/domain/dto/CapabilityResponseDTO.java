package com.example.capability_service.domain.dto;

import com.example.capability_service.infrastructure.adapter.in.TechnologyDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityResponseDTO {
    @JsonIgnore
    private Long id;

    private String name;
    private String description;
    private List<TechnologyDTO> technologies;
}
