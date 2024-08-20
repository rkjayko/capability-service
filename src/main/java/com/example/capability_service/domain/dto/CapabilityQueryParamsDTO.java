package com.example.capability_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityQueryParamsDTO {
    private int page = 0;
    private int size = 10;
    private String sortBy = "name";
    private boolean ascending = true;
}
