package com.example.demo.dto;

import com.example.demo.entity.TermsType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermsRequestDto {
    private TermsType type;
    private String title;
    private String content;
    private String version;
    private boolean isActive;
}