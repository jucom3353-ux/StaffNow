package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkSessionCreateRequestDto {

    private String workDate;

    private String shift;

    private Long jobPostId;
}