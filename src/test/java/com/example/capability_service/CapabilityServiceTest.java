package com.example.capability_service;

import com.example.capability_service.domain.dto.CapabilityQueryParamsDTO;
import com.example.capability_service.domain.dto.CapabilityRequestDTO;
import com.example.capability_service.domain.dto.CapabilityResponseDTO;
import com.example.capability_service.domain.dto.TechnologyDTO;
import com.example.capability_service.domain.entity.Capability;
import com.example.capability_service.infrastructure.adapter.CapabilityRepository;
import com.example.capability_service.domain.useCase.CapabilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation;
import org.springframework.data.relational.core.query.Query;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityServiceTest {

    private static final Long CAPABILITY_ID_1 = 1L;

    private static final Long CAPABILITY_ID_2 = 2L;

    private static final Long CAPABILITY_ID_3= 3L;

    private static final String CAPABILITY_NAME = "new capability";

    private static final String CAPABILITY_DESCRIPTION = "capability description generic";

    @Mock
    private CapabilityRepository repository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private R2dbcEntityTemplate template;

    @InjectMocks
    private CapabilityServiceImpl service;

    @BeforeEach
    void setup() {
        service = new CapabilityServiceImpl(repository, webClientBuilder, template);
    }

    @Test
    void createCapabilitySuccessful() {
        List<Long> technologyIds = Arrays.asList(CAPABILITY_ID_1, CAPABILITY_ID_2, CAPABILITY_ID_3);
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO(null, CAPABILITY_NAME, CAPABILITY_DESCRIPTION, technologyIds);

        TechnologyDTO technology1 = new TechnologyDTO(CAPABILITY_ID_1, "Tech1", "Desc1");
        TechnologyDTO technology2 = new TechnologyDTO(CAPABILITY_ID_2, "Tech2", "Desc2");
        TechnologyDTO technology3 = new TechnologyDTO(CAPABILITY_ID_3, "Tech3", "Desc3");

        Capability savedCapability = new Capability();
        savedCapability.setId(CAPABILITY_ID_1);
        savedCapability.setName(CAPABILITY_NAME);
        savedCapability.setDescription(CAPABILITY_DESCRIPTION);

        when(repository.save(any(Capability.class))).thenReturn(Mono.just(savedCapability));

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TechnologyDTO.class))
                .thenReturn(Mono.just(technology1), Mono.just(technology2), Mono.just(technology3));

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        Mono<CapabilityResponseDTO> result = service.createCapability(requestDTO);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getId().equals(CAPABILITY_ID_1) &&
                        response.getName().equals(CAPABILITY_NAME) &&
                        response.getDescription().equals(CAPABILITY_DESCRIPTION))
                .verifyComplete();
    }

    @Test
    void createCapabilityFailsWhenLessThanThreeTechnologies() {
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO(null, CAPABILITY_NAME, CAPABILITY_DESCRIPTION, Collections.singletonList(CAPABILITY_ID_1));

        Mono<CapabilityResponseDTO> result = service.createCapability(requestDTO);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Una capacidad debe tener al menos 3 tecnologías asociadas."))
                .verify();
    }

    @Test
    void createCapabilityFailsWhenTechnologiesAreDuplicated() {
        List<Long> technologyIds = Arrays.asList(1L, 1L, 2L);
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO(null, CAPABILITY_NAME, CAPABILITY_DESCRIPTION, technologyIds);

        Mono<CapabilityResponseDTO> result = service.createCapability(requestDTO);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Las tecnologías asociadas no pueden estar repetidas."))
                .verify();
    }

    @Test
    void createCapabilityFailsWhenMoreThanTwentyTechnologies() {
        List<Long> technologyIds = LongStream.rangeClosed(1, 21).boxed().collect(Collectors.toList());
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO(null, CAPABILITY_NAME, CAPABILITY_DESCRIPTION, technologyIds);

        Mono<CapabilityResponseDTO> result = service.createCapability(requestDTO);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Una capacidad no puede tener más de 20 tecnologías asociadas."))
                .verify();
    }

    @Test
    void getAllCapabilitiesSuccess() {
        Capability capability = new Capability();
        capability.setId(1L);
        capability.setName("Capability 1");
        capability.setDescription("Description 1");

        WebClient.Builder webClientBuilderMock = mock(WebClient.Builder.class);
        WebClient webClientMock = mock(WebClient.class);
        when(webClientBuilderMock.build()).thenReturn(webClientMock);

        ReactiveSelectOperation.ReactiveSelect<Capability> selectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);
        ReactiveSelectOperation.TerminatingSelect<Capability> terminatingSelectMock = mock(ReactiveSelectOperation.TerminatingSelect.class);

        when(template.select(Capability.class)).thenReturn(selectMock);
        when(selectMock.matching(any(Query.class))).thenReturn(terminatingSelectMock);
        when(terminatingSelectMock.all()).thenReturn(Flux.just(capability));

        when(webClientMock.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(TechnologyDTO.class)).thenReturn(Flux.just(
                new TechnologyDTO(1L, "Tech1", "Description1")
        ));

        CapabilityServiceImpl service = new CapabilityServiceImpl(repository, webClientBuilderMock, template);
        CapabilityQueryParamsDTO queryParams = new CapabilityQueryParamsDTO(0, 10, "name", true);

        Flux<CapabilityResponseDTO> result = service.getAllCapabilities(queryParams);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getId().equals(1L) &&
                        dto.getName().equals("Capability 1") &&
                        dto.getTechnologies().size() == 1)
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
    void getAllCapabilitiesErrorDuringTechnologyFetch() {
        Capability capability = new Capability();
        capability.setId(1L);
        capability.setName("Capability 1");
        capability.setDescription("Description 1");

        ReactiveSelectOperation.ReactiveSelect<Capability> selectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);
        when(template.select(Capability.class)).thenReturn(selectMock);
        when(selectMock.matching(any(Query.class))).thenReturn(selectMock);
        when(selectMock.all()).thenReturn(Flux.just(capability));

        WebClient webClient = mock(WebClient.class);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(TechnologyDTO.class)).thenReturn(Flux.error(new RuntimeException("Technology service error")));

        CapabilityQueryParamsDTO queryParams = new CapabilityQueryParamsDTO(0, 10, "name", true);
        Flux<CapabilityResponseDTO> result = service.getAllCapabilities(queryParams);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Technology service error"))
                .verify();
    }
}
