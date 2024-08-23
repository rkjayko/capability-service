package com.example.capability_service.infrastructure.adapter.in;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class TechnologyServiceAdapter {

    @Value("${capability.technology.service.url}")
    private String capabilityTechnologyServiceUrl;

    @Value("${technology.service.url}")
    private String technologyServiceUrl;

    private final WebClient webClient;

    public TechnologyServiceAdapter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Flux<TechnologyDTO> getTechnologiesByCapability(Long capabilityId) {
        return webClient.get()
                .uri(capabilityTechnologyServiceUrl + capabilityId + "/technologies")
                .retrieve()
                .bodyToFlux(TechnologyDTO.class);
    }

    public Mono<Void> saveCapabilityTechnologies(Long capabilityId, List<Long> technologyIds) {
        return webClient.post()
                .uri(capabilityTechnologyServiceUrl + capabilityId + "/technologies")
                .bodyValue(technologyIds)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Flux<TechnologyDTO> getTechnologiesByIds(List<Long> technologyIds) {
        return Flux.fromIterable(technologyIds)
                .flatMap(id -> webClient.get()
                        .uri(technologyServiceUrl + "/" + id)
                        .retrieve()
                        .bodyToMono(TechnologyDTO.class));
    }
}
