package com.example.capability_service.domain.useCase;

import com.example.capability_service.application.mapper.CapabilityMapper;
import com.example.capability_service.application.mapper.TechnologyMapper;
import com.example.capability_service.domain.dto.CapabilityQueryParamsDTO;
import com.example.capability_service.domain.dto.CapabilityRequestDTO;
import com.example.capability_service.domain.dto.CapabilityResponseDTO;
import com.example.capability_service.domain.entity.Capability;
import com.example.capability_service.infrastructure.adapter.CapabilityRepository;
import com.example.capability_service.infrastructure.adapter.out.TechnologyServiceAdapter;
import com.example.capability_service.infrastructure.config.CustomException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;

@Service
public class CapabilityServiceImpl implements ICapabilityService {

    private static final int MAX_TECHNOLOGIES = 20;

    private static final int MIN_TECHNOLOGIES = 3;

    private static final String ERROR_MIN_TECHNOLOGIES = "Una capacidad debe tener al menos " + MIN_TECHNOLOGIES + " tecnologías asociadas.";

    private static final String ERROR_DUPLICATE_TECHNOLOGIES = "Las tecnologías asociadas no pueden estar repetidas.";

    private static final String ERROR_MAX_TECHNOLOGIES = "Una capacidad no puede tener más de " + MAX_TECHNOLOGIES + " tecnologías asociadas.";

    private final CapabilityRepository repository;

    private final R2dbcEntityTemplate template;

    private final CapabilityMapper capabilityMapper;

    private final TechnologyMapper technologyMapper;

    private final TechnologyServiceAdapter technologyServiceAdapter;

    public CapabilityServiceImpl(CapabilityRepository repository, TechnologyServiceAdapter technologyServiceAdapter, R2dbcEntityTemplate template,
                                 CapabilityMapper capabilityMapper, TechnologyMapper technologyMapper) {
        this.repository = repository;
        this.technologyServiceAdapter = technologyServiceAdapter;
        this.template = template;
        this.capabilityMapper = capabilityMapper;
        this.technologyMapper = technologyMapper;
    }

    @Override
    public Mono<CapabilityResponseDTO> createCapability(CapabilityRequestDTO capabilityRequestDTO) {
        List<Long> technologiesIds = capabilityRequestDTO.getTechnologies();

        return validateTechnologies(technologiesIds)
                .then(technologyServiceAdapter.getTechnologiesByIds(technologiesIds)
                        .collectList()
                        .flatMap(technologies -> capabilityMapper.mapToEntity(capabilityRequestDTO)
                                .flatMap(capability -> repository.save(capability)
                                        .flatMap(savedCapability -> technologyServiceAdapter.saveCapabilityTechnologies(savedCapability.getId(), technologiesIds)
                                                .thenReturn(savedCapability))))
                        .flatMap(capabilityMapper::mapToDTO));
    }

    @Override
    public Flux<CapabilityResponseDTO> getAllCapabilities(CapabilityQueryParamsDTO queryParams) {

        validatePaginationParams(queryParams);

        Pageable pageable = PageRequest.of(queryParams.getPage(), queryParams.getSize(),
                queryParams.isAscending() ? Sort.by(queryParams.getSortBy()).ascending() : Sort.by(queryParams.getSortBy()).descending());

        Query query = Query.empty()
                .limit(pageable.getPageSize())
                .offset((long) pageable.getPageNumber() * pageable.getPageSize());

        return template.select(Capability.class)
                .matching(query)
                .all()
                .flatMap(capability -> technologyServiceAdapter.getTechnologiesByCapability(capability.getId())
                        .collectList()
                        .map(technologies -> technologyMapper.mapToDTOTechnologies(capability, technologies)));
    }

    private Mono<Void> validateTechnologies(List<Long> technologiesIds) {
        if (technologiesIds == null || technologiesIds.size() < MIN_TECHNOLOGIES) {
            return Mono.error(CustomException.CapabilityValidationException.withMessage(ERROR_MIN_TECHNOLOGIES));
        }

        if (technologiesIds.size() != new HashSet<>(technologiesIds).size()) {
            return Mono.error(CustomException.CapabilityValidationException.withMessage(ERROR_DUPLICATE_TECHNOLOGIES));
        }

        if (technologiesIds.size() > MAX_TECHNOLOGIES) {
            return Mono.error(CustomException.CapabilityValidationException.withMessage(ERROR_MAX_TECHNOLOGIES));
        }

        return Mono.empty();
    }

    private void validatePaginationParams(CapabilityQueryParamsDTO queryParams) {
        if (queryParams.getPage() < 0 || queryParams.getSize() <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
    }
}