package com.example.capability_service;

import com.example.capability_service.dto.CapabilityRequestDTO;
import com.example.capability_service.dto.CapabilityResponseDTO;
import com.example.capability_service.dto.TechnologyDTO;
import com.example.capability_service.entity.Capability;
import com.example.capability_service.repository.CapabilityRepository;
import com.example.capability_service.service.CapabilityServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private CapabilityServiceImpl service;

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
    void createCapability_FailsWhenTechnologiesAreDuplicated() {
        List<Long> technologyIds = Arrays.asList(1L, 1L, 2L);
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO(null, CAPABILITY_NAME, CAPABILITY_DESCRIPTION, technologyIds);

        Mono<CapabilityResponseDTO> result = service.createCapability(requestDTO);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Las tecnologías asociadas no pueden estar repetidas."))
                .verify();
    }

    @Test
    void createCapability_FailsWhenMoreThanTwentyTechnologies() {
        List<Long> technologyIds = LongStream.rangeClosed(1, 21).boxed().collect(Collectors.toList());
        CapabilityRequestDTO requestDTO = new CapabilityRequestDTO(null, CAPABILITY_NAME, CAPABILITY_DESCRIPTION, technologyIds);

        Mono<CapabilityResponseDTO> result = service.createCapability(requestDTO);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Una capacidad no puede tener más de 20 tecnologías asociadas."))
                .verify();
    }
}
