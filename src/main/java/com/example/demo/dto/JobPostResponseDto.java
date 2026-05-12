package com.example.demo.dto;

public class JobPostResponseDto {

    private Long id;

    private String title;

    private String content;

    private String companyName;

    // 공고 목록 조회용
    public JobPostResponseDto(
            Long id,
            String title,
            String content,
            String companyName
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.companyName = companyName;
    }

    // 공고 단순 조회용
    public JobPostResponseDto(
            String title,
            String content
    ) {
        this.title = title;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCompanyName() {
        return companyName;
    }
}