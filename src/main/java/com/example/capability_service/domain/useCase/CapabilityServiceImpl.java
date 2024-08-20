package com.example.capability_service.domain.useCase;

import com.example.capability_service.domain.dto.CapabilityQueryParamsDTO;
import com.example.capability_service.domain.dto.CapabilityRequestDTO;
import com.example.capability_service.domain.dto.CapabilityResponseDTO;
import com.example.capability_service.domain.dto.TechnologyDTO;
import com.example.capability_service.domain.entity.Capability;
import com.example.capability_service.infrastructure.adapter.CapabilityRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CapabilityServiceImpl implements ICapabilityService {

    private static final String TECHNOLOGY_SERVICE_URL = "http://localhost:8080/api/technologies";

    private static final String CAPABILITY_TECHNOLOGY_SERVICE_URL = TECHNOLOGY_SERVICE_URL + "/capabilities/";

    private final CapabilityRepository repository;

    private final WebClient.Builder webClientBuilder;

    private final R2dbcEntityTemplate template;

    public CapabilityServiceImpl(CapabilityRepository repository, WebClient.Builder webClientBuilder, R2dbcEntityTemplate template) {
        this.repository = repository;
        this.webClientBuilder = webClientBuilder;
        this.template = template;
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

    @Override
    public Flux<CapabilityResponseDTO> getAllCapabilities(CapabilityQueryParamsDTO queryParams) {

        if (queryParams.getPage() < 0 || queryParams.getSize() <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        Pageable pageable = PageRequest.of(queryParams.getPage(), queryParams.getSize(),
                queryParams.isAscending() ? Sort.by(queryParams.getSortBy()).ascending() : Sort.by(queryParams.getSortBy()).descending());

        Query query = Query.empty()
                .limit(pageable.getPageSize())
                .offset((long) pageable.getPageNumber() * pageable.getPageSize());

        return template.select(Capability.class)
                .matching(query)
                .all()
                .flatMap(capability -> getTechnologiesByCapability(capability.getId())
                        .collectList()
                        .map(technologies -> mapToDTOTechnologies(capability, technologies)));
    }

    private CapabilityResponseDTO mapToDTOTechnologies(Capability capability, List<TechnologyDTO> technologies) {
        CapabilityResponseDTO dto = new CapabilityResponseDTO();
        dto.setId(capability.getId());
        dto.setName(capability.getName());
        dto.setDescription(capability.getDescription());
        dto.setTechnologies(technologies.stream()
                .map(tech -> new TechnologyDTO(tech.getId(), tech.getName(), tech.getDescription()))
                .collect(Collectors.toList()));
        return dto;
    }

    private Flux<TechnologyDTO> getTechnologiesByCapability(Long capabilityId) {
        WebClient webClient = webClientBuilder.build();
        return webClient.get()
                .uri(CAPABILITY_TECHNOLOGY_SERVICE_URL + capabilityId + "/technologies" )
                .retrieve()
                .bodyToFlux(TechnologyDTO.class);
    }

    private Mono<Void> saveCapabilityTechnologies(Long capabilityId, List<Long> technologyIds) {
        return webClientBuilder.build().post()
                .uri(CAPABILITY_TECHNOLOGY_SERVICE_URL + capabilityId + "/technologies")
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
                        .uri(TECHNOLOGY_SERVICE_URL + "/" + id)
                        .retrieve()
                        .bodyToMono(TechnologyDTO.class));
    }
}