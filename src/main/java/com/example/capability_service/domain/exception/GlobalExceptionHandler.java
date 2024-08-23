package com.example.capability_service.domain.exception;

import com.example.capability_service.infrastructure.adapter.out.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CapabilityException.CapabilityValidationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleTechnologyNotFound(CapabilityException.CapabilityValidationException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                ex.getMessage());
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT));
    }
}