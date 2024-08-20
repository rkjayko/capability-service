package com.example.capability_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityResponseDTO {
    private Long id;
    private String name;
    private String description;
    private List<TechnologyDTO> technologies;
}
