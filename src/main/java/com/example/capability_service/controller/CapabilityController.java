package com.example.capability_service.controller;

import com.example.capability_service.dto.CapabilityRequestDTO;
import com.example.capability_service.dto.CapabilityResponseDTO;
import com.example.capability_service.service.ICapabilityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/capabilities")
public class CapabilityController {

    private final ICapabilityService capabilityService;

    public CapabilityController(ICapabilityService capabilityService) {
        this.capabilityService = capabilityService;
    }

    @PostMapping
    public Mono<CapabilityResponseDTO> createCapability(@RequestBody @Valid CapabilityRequestDTO capabilityDTO) {
        return capabilityService.createCapability(capabilityDTO);
    }
}