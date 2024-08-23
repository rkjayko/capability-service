package com.example.capability_service.domain.port;

import com.example.capability_service.domain.entity.CapabilityBootcamp;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CapabilityBootcampRepository extends ReactiveCrudRepository<CapabilityBootcamp, Long> {
    Flux<CapabilityBootcamp> findByBootcampId(Long bootcampId);
}
