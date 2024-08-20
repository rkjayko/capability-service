package com.example.capability_service.domain.useCase;

import com.example.capability_service.domain.dto.CapabilityQueryParamsDTO;
import com.example.capability_service.domain.dto.CapabilityRequestDTO;
import com.example.capability_service.domain.dto.CapabilityResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICapabilityService {
    Mono<CapabilityResponseDTO> createCapability(CapabilityRequestDTO capabilityDTO);
    Flux<CapabilityResponseDTO> getAllCapabilities(CapabilityQueryParamsDTO queryParams);
}