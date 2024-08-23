package com.example.capability_service.infrastructure.controller;

import com.example.capability_service.infrastructure.adapter.in.CapabilityBootcampRequestDTO;
import com.example.capability_service.domain.dto.CapabilityRequestDTO;
import com.example.capability_service.domain.dto.CapabilityResponseDTO;
import com.example.capability_service.application.service.ICapabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/capabilities")
@Tag(name = "Capability", description = "Capability management APIs")
public class CapabilityController {

    private final ICapabilityService capabilityService;

    public CapabilityController(ICapabilityService capabilityService) {
        this.capabilityService = capabilityService;
    }

    @PostMapping
    @Operation(summary = "Create a new capability", description = "Creates a new capability with the provided details")
    public Mono<Void> createCapability(@RequestBody @Valid CapabilityRequestDTO capabilityDTO) {
        return capabilityService.createCapability(capabilityDTO);
    }

    @GetMapping
    @Operation(summary = "Get all Capabilities", description = "Retrieves all Capabilities")
    public Flux<CapabilityResponseDTO> getAllCapabilities(
            @RequestParam(name = "sort", defaultValue = "asc") String sortDirection,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        return capabilityService.getAllCapabilities(sortDirection, page, size);
    }

    @PostMapping("/capability-bootcamp")
    @Operation(summary = "Save capability bootcamp relation", description = "Save in an entity the id from capability and id from bootcamp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> saveCapabilityBootcampRelation(@RequestBody @Valid CapabilityBootcampRequestDTO request) {
        return capabilityService.saveCapabilityBootcampRelation(request);
    }

    @GetMapping("/{bootcampId}")
    @Operation(summary = "Get one or more capabilities by bootcamp ID", description = "Retrieves a capability/ies by its ID")
    public Flux<CapabilityResponseDTO> getCapabilitiesByBootcampId(@PathVariable Long bootcampId) {
        return capabilityService.findCapabilitiesByBootcampId(bootcampId);
    }
}