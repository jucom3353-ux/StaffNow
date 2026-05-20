package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JobPostRoleRequestDto {
    private String roleName;
    private Integer wageAmount;
    private Integer recruitCount;
    private Boolean requiresExperience = false;
}