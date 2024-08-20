package com.example.capability_service.infrastructure.config;

public class CustomException {

    public static class CapabilityValidationException extends RuntimeException {
        public CapabilityValidationException(String message) {
            super(message);
        }

        public static CapabilityValidationException withMessage(String message) {
            return new CapabilityValidationException(message);
        }
    }
}
