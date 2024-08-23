package com.example.capability_service.infrastructure.adapter.in;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityBootcampRequestDTO {
    @NotNull
    private Long bootcampId;
    @NotEmpty
    private List<Long> capabilityIds;

}