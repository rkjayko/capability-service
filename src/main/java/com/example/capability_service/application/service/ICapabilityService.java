package com.example.capability_service.application.service;

import com.example.capability_service.infrastructure.adapter.in.CapabilityBootcampRequestDTO;
import com.example.capability_service.domain.dto.CapabilityRequestDTO;
import com.example.capability_service.domain.dto.CapabilityResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICapabilityService {
    Mono<Void> createCapability(CapabilityRequestDTO capabilityDTO);

    Flux<CapabilityResponseDTO> getAllCapabilities(String sortDirection, int page, int size);

    Mono<Void> saveCapabilityBootcampRelation(CapabilityBootcampRequestDTO request);

    Flux<CapabilityResponseDTO> findCapabilitiesByBootcampId(Long bootcampId);
}