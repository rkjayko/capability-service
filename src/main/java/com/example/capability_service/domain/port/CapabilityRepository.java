package com.example.capability_service.domain.port;

import com.example.capability_service.domain.entity.Capability;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CapabilityRepository extends ReactiveCrudRepository<Capability, Long> {
    Mono<Capability> findByName(String name);
    Flux<Capability> findAllBy(Pageable pageable);
}