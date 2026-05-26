package com.example.demo.dto;

import com.example.demo.entity.Terms;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TermsResponseDto {

    private Long id;
    private String type;
    private String title;
    private String content;
    private String version;
    private LocalDateTime updatedAt;

    public TermsResponseDto(Terms terms) {
        this.id = terms.getId();
        this.type = terms.getType().name();
        this.title = terms.getTitle();
        this.content = terms.getContent();
        this.version = terms.getVersion();
        this.updatedAt = terms.getUpdatedAt();
    }
}