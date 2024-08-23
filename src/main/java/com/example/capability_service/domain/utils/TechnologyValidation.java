package com.example.capability_service.domain.utils;

import com.example.capability_service.domain.exception.CapabilityException;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;

public class TechnologyValidation {

    private static final int MAX_TECHNOLOGIES = 20;

    private static final int MIN_TECHNOLOGIES = 3;

    private static final String ERROR_MIN_TECHNOLOGIES = "Una capacidad debe tener al menos " + MIN_TECHNOLOGIES + " tecnologías asociadas.";

    private static final String ERROR_DUPLICATE_TECHNOLOGIES = "Las tecnologías asociadas no pueden estar repetidas.";

    private static final String ERROR_MAX_TECHNOLOGIES = "Una capacidad no puede tener más de " + MAX_TECHNOLOGIES + " tecnologías asociadas.";

    public static Mono<Void> validateTechnologies(List<Long> technologiesIds) {
        if (technologiesIds == null || technologiesIds.size() < MIN_TECHNOLOGIES) {
            return Mono.error(CapabilityException.CapabilityValidationException.withMessage(ERROR_MIN_TECHNOLOGIES));
        }

        if (technologiesIds.size() != new HashSet<>(technologiesIds).size()) {
            return Mono.error(CapabilityException.CapabilityValidationException.withMessage(ERROR_DUPLICATE_TECHNOLOGIES));
        }

        if (technologiesIds.size() > MAX_TECHNOLOGIES) {
            return Mono.error(CapabilityException.CapabilityValidationException.withMessage(ERROR_MAX_TECHNOLOGIES));
        }
        return Mono.empty();
    }
}
