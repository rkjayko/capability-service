package com.example.capability_service.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.example.capability_service.domain.utils.ValidationMessages.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityRequestDTO {

    private Long id;

    @NotBlank(message = NAME_NOT_BLANK)
    @Size(max = 50, message = NAME_SIZE)
    private String name;

    @NotBlank(message = DESCRIPTION_NOT_BLANK)
    @Size(max = 90, message = DESCRIPTION_SIZE)
    private String description;

    private List<Long> technologies;
}
