package com.example.capability_service.infrastructure.adapter.in;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnologyDTO {
    @JsonIgnore
    private Long id;

    @NotBlank(message = "El nombre de la tecnología no puede estar vacío")
    @Size(max = 50, message = "El nombre de la tecnología no puede tener más de 50 caracteres")
    private String name;

    @NotBlank(message = "La descripción de la tecnología no puede estar vacía")
    @Size(max = 90, message = "La descripción de la tecnología no puede tener más de 90 caracteres")
    private String description;
}