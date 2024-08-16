package com.example.capability_service.repository;

import com.example.capability_service.entity.Capability;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface CapabilityRepository extends R2dbcRepository<Capability, Long> {
}