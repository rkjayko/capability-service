package com.example.capability_service;

import com.example.capability_service.application.mapper.CapabilityMapper;
import com.example.capability_service.application.mapper.TechnologyMapper;
import com.example.capability_service.domain.dto.CapabilityQueryParamsDTO;
import com.example.capability_service.domain.dto.CapabilityRequestDTO;
import com.example.capability_service.domain.dto.CapabilityResponseDTO;
import com.example.capability_service.domain.dto.TechnologyDTO;
import com.example.capability_service.domain.entity.Capability;
import com.example.capability_service.infrastructure.adapter.CapabilityRepository;
import com.example.capability_service.domain.useCase.CapabilityServiceImpl;
import com.example.capability_service.infrastructure.adapter.out.TechnologyServiceAdapter;
import com.example.capability_service.infrastructure.config.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityServiceTest {

    private static final int MAX_TECHNOLOGIES = 20;

    private static final int MIN_TECHNOLOGIES = 3;

    private static final String ERROR_MIN_TECHNOLOGIES = "Una capacidad debe tener al menos " + MIN_TECHNOLOGIES + " tecnologías asociadas.";

    private static final String ERROR_DUPLICATE_TECHNOLOGIES = "Las tecnologías asociadas no pueden estar repetidas.";

    private static final String ERROR_MAX_TECHNOLOGIES = "Una capacidad no puede tener más de " + MAX_TECHNOLOGIES + " tecnologías asociadas.";

    private static final Long CAPABILITY_ID_1 = 1L;

    private static final Long CAPABILITY_ID_2 = 2L;

    private static final Long CAPABILITY_ID_3= 3L;

    private static final String CAPABILITY_NAME = "new capability";

    private static final String CAPABILITY_DESCRIPTION = "capability description generic";

    private static final String TECHNOLOGY_NAME = "Java";

    private static final Long TECHNOLOGY_ID_1 = 1L;

    private static final Long TECHNOLOGY_ID_2 = 2L;

    private static final String TECHNOLOGY_DESCRIPTION = "Programming language";

    @Mock
    private CapabilityRepository repository;

    @Mock
    private R2dbcEntityTemplate template;

    @Mock
    private TechnologyMapper technologyMapper;

    @Mock
    private CapabilityMapper capabilityMapper;

    @Mock
    private TechnologyServiceAdapter technologyServiceAdapter;

    @InjectMocks
    private CapabilityServiceImpl service;

    @BeforeEach
    void setup() {
        service = new CapabilityServiceImpl(repository, technologyServiceAdapter, template, capabilityMapper, technologyMapper);
    }

    @Test
    void createCapabilitySuccessful() {
        List<Long> technologyIds = Arrays.asList(TECHNOLOGY_ID_1, TECHNOLOGY_ID_2, CAPABILITY_ID_3);
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO(null, CAPABILITY_NAME, CAPABILITY_DESCRIPTION, technologyIds);

        Capability savedCapability = new Capability();
        savedCapability.setId(CAPABILITY_ID_1);
        savedCapability.setName(CAPABILITY_NAME);
        savedCapability.setDescription(CAPABILITY_DESCRIPTION);

        CapabilityResponseDTO responseDTO = new CapabilityResponseDTO();
        responseDTO.setId(CAPABILITY_ID_1);
        responseDTO.setName(CAPABILITY_NAME);
        responseDTO.setDescription(CAPABILITY_DESCRIPTION);

        when(capabilityMapper.mapToEntity(any(CapabilityRequestDTO.class))).thenReturn(Mono.just(savedCapability));
        when(repository.save(any(Capability.class))).thenReturn(Mono.just(savedCapability));
        when(capabilityMapper.mapToDTO(any(Capability.class))).thenReturn(Mono.just(responseDTO));

        when(technologyServiceAdapter.getTechnologiesByIds(anyList()))
                .thenReturn(Flux.just(
                        new TechnologyDTO(TECHNOLOGY_ID_1, "Tech1", "Desc1"),
                        new TechnologyDTO(TECHNOLOGY_ID_2, "Tech2", "Desc2"),
                        new TechnologyDTO(3L, "Tech3", "Desc3")
                ));

        when(technologyServiceAdapter.saveCapabilityTechnologies(any(Long.class), anyList()))
                .thenReturn(Mono.empty());

        Mono<CapabilityResponseDTO> result = service.createCapability(requestDTO);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getId().equals(CAPABILITY_ID_1) &&
                        response.getName().equals(CAPABILITY_NAME) &&
                        response.getDescription().equals(CAPABILITY_DESCRIPTION))
                .verifyComplete();
    }


    @Test
    void createCapabilityFailsWhenLessThanThreeTechnologies() {
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO(null, CAPABILITY_NAME, CAPABILITY_DESCRIPTION, Collections.singletonList(1L));

        TechnologyServiceAdapter technologyServiceAdapter = mock(TechnologyServiceAdapter.class);
        when(technologyServiceAdapter.getTechnologiesByIds(anyList()))
                .thenReturn(Flux.empty());

        CapabilityServiceImpl service = new CapabilityServiceImpl(repository, technologyServiceAdapter, template, capabilityMapper, technologyMapper);

        Mono<CapabilityResponseDTO> result = service.createCapability(requestDTO);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomException.CapabilityValidationException &&
                        throwable.getMessage().equals(ERROR_MIN_TECHNOLOGIES))
                .verify();
    }

    @Test
    void createCapabilityFailsWhenTechnologiesAreDuplicated() {
        List<Long> technologyIds = Arrays.asList(TECHNOLOGY_ID_1, TECHNOLOGY_ID_1, TECHNOLOGY_ID_2);
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO(null, CAPABILITY_NAME, CAPABILITY_DESCRIPTION, technologyIds);

        when(technologyServiceAdapter.getTechnologiesByIds(anyList())).thenReturn(Flux.fromIterable(technologyIds)
                .flatMap(id -> Mono.just(new TechnologyDTO(id, TECHNOLOGY_NAME, TECHNOLOGY_DESCRIPTION))));

        Mono<CapabilityResponseDTO> result = service.createCapability(requestDTO);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomException.CapabilityValidationException &&
                        throwable.getMessage().equals(ERROR_DUPLICATE_TECHNOLOGIES))
                .verify();
    }

    @Test
    void createCapabilityFailsWhenMoreThanTwentyTechnologies() {
        List<Long> technologyIds = LongStream.rangeClosed(1, 21).boxed().collect(Collectors.toList());
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO(null, CAPABILITY_NAME, CAPABILITY_DESCRIPTION, technologyIds);

        when(technologyServiceAdapter.getTechnologiesByIds(anyList()))
                .thenReturn(Flux.fromIterable(technologyIds)
                        .flatMap(id -> Mono.just(new TechnologyDTO(id, TECHNOLOGY_NAME, TECHNOLOGY_DESCRIPTION))));

        Mono<CapabilityResponseDTO> result = service.createCapability(requestDTO);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomException.CapabilityValidationException &&
                        throwable.getMessage().equals(ERROR_MAX_TECHNOLOGIES))
                .verify();
    }

    @Test
    void getAllCapabilitiesSuccessful() {
        Capability capability1 = new Capability();
        capability1.setId(1L);
        capability1.setName("Capability 1");
        capability1.setDescription("Description 1");

        Capability capability2 = new Capability();
        capability2.setId(2L);
        capability2.setName("Capability 2");
        capability2.setDescription("Description 2");

        List<Capability> capabilities = Arrays.asList(capability1, capability2);

        TechnologyDTO tech1 = new TechnologyDTO(1L, TECHNOLOGY_NAME, TECHNOLOGY_DESCRIPTION);
        TechnologyDTO tech2 = new TechnologyDTO(2L, "Tech2", "Desc2");

        CapabilityResponseDTO dto1 = new CapabilityResponseDTO( CAPABILITY_ID_1, CAPABILITY_NAME,CAPABILITY_DESCRIPTION, Arrays.asList(tech1, tech2));
        CapabilityResponseDTO dto2 = new CapabilityResponseDTO( CAPABILITY_ID_2, "Capability 2","Description 2", Arrays.asList(tech1, tech2));

        ReactiveSelectOperation.ReactiveSelect<Capability> selectOperation = mock(ReactiveSelectOperation.ReactiveSelect.class);
        when(template.select(Capability.class)).thenReturn(selectOperation);
        when(selectOperation.matching(any(Query.class))).thenReturn(selectOperation);
        when(selectOperation.all()).thenReturn(Flux.fromIterable(capabilities));

        when(technologyServiceAdapter.getTechnologiesByCapability(any(Long.class)))
                .thenReturn(Flux.just(tech1, tech2));

        Map<Long, CapabilityResponseDTO> capabilityToDTOMap = new HashMap<>();
        capabilityToDTOMap.put(1L, dto1);
        capabilityToDTOMap.put(2L, dto2);

        when(technologyMapper.mapToDTOTechnologies(any(Capability.class), anyList()))
                .thenAnswer(invocation -> {
                    Capability capability = invocation.getArgument(0);
                    return capabilityToDTOMap.getOrDefault(capability.getId(), null);
                });

        CapabilityQueryParamsDTO queryParams = new CapabilityQueryParamsDTO(0,10,"name",true);
        Flux<CapabilityResponseDTO> result = service.getAllCapabilities(queryParams);

        StepVerifier.create(result)
                .expectNext(dto1, dto2)
                .verifyComplete();
    }

    @Test
    void getAllCapabilitiesEmptyResult() {
        ReactiveSelectOperation.ReactiveSelect<Capability> selectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);

        when(template.select(Capability.class)).thenReturn(selectMock);
        when(selectMock.matching(any(Query.class))).thenReturn(selectMock);
        when(selectMock.all()).thenReturn(Flux.empty());

        CapabilityQueryParamsDTO queryParams = new CapabilityQueryParamsDTO(0, 10, "name", true);

        Flux<CapabilityResponseDTO> result = service.getAllCapabilities(queryParams);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getAllCapabilitiesErrorDuringTechnologySearch() {
        Capability capability = new Capability();
        capability.setId(CAPABILITY_ID_1);
        capability.setName(CAPABILITY_NAME);
        capability.setDescription(CAPABILITY_DESCRIPTION);

        ReactiveSelectOperation.ReactiveSelect<Capability> selectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);
        when(template.select(Capability.class)).thenReturn(selectMock);
        when(selectMock.matching(any(Query.class))).thenReturn(selectMock);
        when(selectMock.all()).thenReturn(Flux.just(capability));

        when(technologyServiceAdapter.getTechnologiesByCapability(any(Long.class)))
                .thenReturn(Flux.error(new RuntimeException("Technology service error")));

        CapabilityQueryParamsDTO queryParams = new CapabilityQueryParamsDTO(0, 10, "name", true);
        Flux<CapabilityResponseDTO> result = service.getAllCapabilities(queryParams);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Technology service error"))
                .verify();
    }
}
