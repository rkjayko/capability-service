package com.example.capability_service.application.service;

import com.example.capability_service.application.mapper.CapabilityMapper;
import com.example.capability_service.application.mapper.TechnologyMapper;
import com.example.capability_service.domain.entity.Capability;
import com.example.capability_service.domain.exception.CapabilityException;
import com.example.capability_service.domain.utils.TechnologyValidation;
import com.example.capability_service.infrastructure.adapter.in.CapabilityBootcampRequestDTO;
import com.example.capability_service.domain.dto.CapabilityRequestDTO;
import com.example.capability_service.domain.dto.CapabilityResponseDTO;
import com.example.capability_service.domain.entity.CapabilityBootcamp;
import com.example.capability_service.domain.port.CapabilityBootcampRepository;
import com.example.capability_service.domain.port.CapabilityRepository;
import com.example.capability_service.infrastructure.adapter.in.TechnologyServiceAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.capability_service.domain.utils.ValidationMessages.CAPABILITY_EXISTS;
import static com.example.capability_service.domain.utils.ValidationMessages.CAPABILITY_EXIST_VALIDATE;

@Service
@Slf4j
public class CapabilityServiceImpl implements ICapabilityService {

    private final CapabilityRepository repository;

    private final CapabilityMapper capabilityMapper;

    private final TechnologyMapper technologyMapper;

    private final TechnologyServiceAdapter technologyServiceAdapter;

    private final CapabilityBootcampRepository capabilityBootcampRepository;

    public CapabilityServiceImpl(CapabilityRepository repository, TechnologyServiceAdapter technologyServiceAdapter, R2dbcEntityTemplate template,
                                 CapabilityMapper capabilityMapper, TechnologyMapper technologyMapper, CapabilityBootcampRepository capabilityBootcampRepository) {
        this.repository = repository;
        this.technologyServiceAdapter = technologyServiceAdapter;
        this.capabilityMapper = capabilityMapper;
        this.technologyMapper = technologyMapper;
        this.capabilityBootcampRepository = capabilityBootcampRepository;
    }

    @Override
    public Mono<Void> createCapability(CapabilityRequestDTO capabilityRequestDTO) {
        log.info("Access to create Technology");
        List<Long> technologiesIds = capabilityRequestDTO.getTechnologies();

        return TechnologyValidation.validateTechnologies(technologiesIds)
                .then(repository.findByName(capabilityRequestDTO.getName())
                        .flatMap(existingCapability -> Mono.error(new CapabilityException.CapabilityValidationException(CAPABILITY_EXISTS)))
                        .switchIfEmpty(Mono.defer(() ->
                                technologyServiceAdapter.getTechnologiesByIds(technologiesIds)
                                        .collectList()
                                        .flatMap(technologies -> capabilityMapper.mapToEntity(capabilityRequestDTO)
                                                .flatMap(capability -> repository.save(capability)
                                                        .flatMap(savedCapability -> technologyServiceAdapter.saveCapabilityTechnologies(savedCapability.getId(), technologiesIds)
                                                                .thenReturn(savedCapability))))
                                        .flatMap(capabilityMapper::mapToDTO)
                                        .onErrorResume(WebClientResponseException.class, ex -> {
                                            String errorMessage = String.format(ex.getResponseBodyAsString());
                                            return Mono.error(new CapabilityException.CapabilityValidationException(errorMessage));
                                        }))
                        ).then());
    }

    @Override
    public Flux<CapabilityResponseDTO> getAllCapabilities(String sortDirection, int page, int size) {
        log.info("Access to get all Capabilities");
        Sort sort = "desc".equalsIgnoreCase(sortDirection) ? Sort.by("name").descending() : Sort.by("name").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return repository.findAllBy(pageable)
                .flatMap(capability -> technologyServiceAdapter.getTechnologiesByCapability(capability.getId())
                        .collectList()
                        .map(technologies -> technologyMapper.mapToDTOTechnologies(capability, technologies)));
    }

    @Override
    public Mono<Void> saveCapabilityBootcampRelation(CapabilityBootcampRequestDTO request) {

        log.info("Save capability bootcamp relation on database");
        List<Long> capabilityIds = request.getCapabilityIds();

        return repository.findAllById(capabilityIds)
                .collectList()
                .flatMap(existingCapabilities -> {
                    Set<Long> existingCapabilityIds = existingCapabilities.stream()
                            .map(Capability::getId)
                            .collect(Collectors.toSet());

                    if (existingCapabilityIds.containsAll(capabilityIds)) {
                        List<CapabilityBootcamp> relationships = capabilityIds.stream()
                                .map(capabilityId -> {
                                    CapabilityBootcamp relationship = new CapabilityBootcamp();
                                    relationship.setCapabilityId(capabilityId);
                                    relationship.setBootcampId(request.getBootcampId());
                                    return relationship;
                                })
                                .collect(Collectors.toList());

                        return capabilityBootcampRepository.saveAll(relationships).then();
                    } else {
                        return Mono.error(new CapabilityException.CapabilityValidationException(CAPABILITY_EXIST_VALIDATE));
                    }
                });
    }

    @Override
    public Flux<CapabilityResponseDTO> findCapabilitiesByBootcampId(Long bootcampId) {
        return capabilityBootcampRepository.findByBootcampId(bootcampId)
                .map(CapabilityBootcamp::getCapabilityId)
                .distinct()
                .flatMap(capabilityId -> repository.findById(capabilityId)
                        .flatMap(capability -> technologyServiceAdapter.getTechnologiesByCapability(capabilityId)
                                .collectList()
                                .flatMap(technologies -> capabilityMapper.mapToDTO(capability)
                                        .map(capabilityDTO -> {
                                            capabilityDTO.setTechnologies(technologies);
                                            return capabilityDTO;
                                        })
                                )
                        )
                );
    }
}