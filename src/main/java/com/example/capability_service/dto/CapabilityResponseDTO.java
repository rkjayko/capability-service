package com.example.capability_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityResponseDTO {
    private Long id;
    private String name;
    private String description;
}
