package com.example.capability_service.application.mapper;

import com.example.capability_service.domain.dto.CapabilityRequestDTO;
import com.example.capability_service.domain.dto.CapabilityResponseDTO;
import com.example.capability_service.domain.entity.Capability;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CapabilityMapper {

    public Mono<CapabilityResponseDTO> mapToDTO(Capability capability) {
        CapabilityResponseDTO dto = new CapabilityResponseDTO();
        dto.setId(capability.getId());
        dto.setName(capability.getName());
        dto.setDescription(capability.getDescription());
        return Mono.just(dto);
    }

    public Mono<Capability> mapToEntity(CapabilityRequestDTO dto) {
        Capability capability = new Capability();
        capability.setId(dto.getId());
        capability.setName(dto.getName());
        capability.setDescription(dto.getDescription());
        return Mono.just(capability);
    }
}
