package com.example.capability_service.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("capabilities")
public class Capability {
    @Id
    private Long id;
    private String name;
    private String description;
}