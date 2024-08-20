package com.example.capability_service.infrastructure.config;

import com.example.capability_service.domain.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.CapabilityValidationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleTechnologyNotFound(CustomException.CapabilityValidationException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                ex.getMessage());
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT));
    }
}