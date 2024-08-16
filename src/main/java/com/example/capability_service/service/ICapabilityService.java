package com.example.capability_service.service;

import com.example.capability_service.dto.CapabilityRequestDTO;
import com.example.capability_service.dto.CapabilityResponseDTO;
import reactor.core.publisher.Mono;

public interface ICapabilityService {
    Mono<CapabilityResponseDTO> createCapability(CapabilityRequestDTO capabilityDTO);
}