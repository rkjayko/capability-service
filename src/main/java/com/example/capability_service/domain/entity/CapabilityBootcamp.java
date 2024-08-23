package com.example.capability_service.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("capability_bootcamp")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityBootcamp {

    @Column("capability_id")
    private Long capabilityId;

    @Column("bootcamp_id")
    private Long bootcampId;

}