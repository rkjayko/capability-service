package com.example.capability_service.infrastructure.controller;

import com.example.capability_service.domain.dto.CapabilityQueryParamsDTO;
import com.example.capability_service.domain.dto.CapabilityRequestDTO;
import com.example.capability_service.domain.dto.CapabilityResponseDTO;
import com.example.capability_service.domain.useCase.ICapabilityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
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

    @GetMapping
    public Flux<CapabilityResponseDTO> getAllCapabilities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending) {

        CapabilityQueryParamsDTO queryParams = new CapabilityQueryParamsDTO(page, size, sortBy, ascending);
        return capabilityService.getAllCapabilities(queryParams);
    }
}