package com.example.capability_service.domain.exception;

public class CapabilityException {

    public static class CapabilityValidationException extends RuntimeException {
        public CapabilityValidationException(String message) {
            super(message);
        }

        public static CapabilityValidationException withMessage(String message) {
            return new CapabilityValidationException(message);
        }
    }
}
