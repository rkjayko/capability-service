package com.example.capability_service.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityRequestDTO {

    private Long id;

    @NotBlank(message = "El nombre de la capacidad no puede estar vacío")
    @Size(max = 50, message = "El nombre de la capacidad no puede tener más de 50 caracteres")
    private String name;

    @NotBlank(message = "La descripción de la capacidad no puede estar vacía")
    @Size(max = 90, message = "La descripción de la capacidad no puede tener más de 90 caracteres")
    private String description;

    private List<Long> technologies;
}
