package com.example.capability_service.service;

import com.example.capability_service.dto.CapabilityRequestDTO;
import com.example.capability_service.dto.CapabilityResponseDTO;
import com.example.capability_service.dto.TechnologyDTO;
import com.example.capability_service.entity.Capability;
import com.example.capability_service.repository.CapabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;

@Service
public class CapabilityServiceImpl implements ICapabilityService {

    private final String technologyServiceUrl = "http://localhost:8080/api/technologies";

    private final CapabilityRepository repository;

    private final WebClient.Builder webClientBuilder;

    public CapabilityServiceImpl(CapabilityRepository repository, WebClient.Builder webClientBuilder) {
        this.repository = repository;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<CapabilityResponseDTO> createCapability(CapabilityRequestDTO capabilityRequestDTO) {

        List<Long> technologiesIds = capabilityRequestDTO.getTechnologies();

        if (technologiesIds == null || technologiesIds.size() < 3) {
            return Mono.error(new IllegalArgumentException("Una capacidad debe tener al menos 3 tecnologías asociadas."));
        }

        if (technologiesIds.size() != new HashSet<>(technologiesIds).size()) {
            return Mono.error(new IllegalArgumentException("Las tecnologías asociadas no pueden estar repetidas."));
        }

        if (technologiesIds.size() > 20) {
            return Mono.error(new IllegalArgumentException("Una capacidad no puede tener más de 20 tecnologías asociadas."));
        }

        return getTechnologiesByIds(capabilityRequestDTO.getTechnologies())
                .collectList()
                .flatMap(technologies -> {
                    Capability capability = mapToEntity(capabilityRequestDTO);
                    return repository.save(capability)
                            .flatMap(savedCapability -> saveCapabilityTechnologies(savedCapability.getId(), capabilityRequestDTO.getTechnologies())
                                    .thenReturn(savedCapability));
                })
                .flatMap(this::mapToDTO);
    }

    private Mono<Void> saveCapabilityTechnologies(Long capabilityId, List<Long> technologyIds) {
        return webClientBuilder.build().post()
                .uri(technologyServiceUrl + "/" + capabilityId + "/technologies")
                .bodyValue(technologyIds)
                .retrieve()
                .bodyToMono(Void.class);
    }

    private Mono<CapabilityResponseDTO> mapToDTO(Capability capability) {
        CapabilityResponseDTO dto = new CapabilityResponseDTO();
        dto.setId(capability.getId());
        dto.setName(capability.getName());
        dto.setDescription(capability.getDescription());
        return Mono.just(dto);
    }

    private Capability mapToEntity(CapabilityRequestDTO dto) {
        Capability capability = new Capability();
        capability.setId(dto.getId());
        capability.setName(dto.getName());
        capability.setDescription(dto.getDescription());
        return capability;
    }

    private Flux<TechnologyDTO> getTechnologiesByIds(List<Long> technologyIds) {
        WebClient webClient = webClientBuilder.build();
        return Flux.fromIterable(technologyIds)
                .flatMap(id -> webClient.get()
                        .uri(technologyServiceUrl + "/" + id)
                        .retrieve()
                        .bodyToMono(TechnologyDTO.class));
    }
}