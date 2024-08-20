package com.example.capability_service.infrastructure.adapter;

import com.example.capability_service.domain.entity.Capability;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CapabilityRepository extends R2dbcRepository<Capability, Long> {
}