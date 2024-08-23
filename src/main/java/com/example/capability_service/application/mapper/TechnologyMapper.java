package com.example.capability_service.application.mapper;

import com.example.capability_service.domain.dto.CapabilityResponseDTO;
import com.example.capability_service.infrastructure.adapter.in.TechnologyDTO;
import com.example.capability_service.domain.entity.Capability;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TechnologyMapper {

    public CapabilityResponseDTO mapToDTOTechnologies(Capability capability, List<TechnologyDTO> technologies) {
        CapabilityResponseDTO dto = new CapabilityResponseDTO();
        dto.setId(capability.getId());
        dto.setName(capability.getName());
        dto.setDescription(capability.getDescription());
        dto.setTechnologies(technologies.stream()
                .map(tech -> new TechnologyDTO(tech.getId(), tech.getName(), tech.getDescription()))
                .collect(Collectors.toList()));
        return dto;
    }
}
