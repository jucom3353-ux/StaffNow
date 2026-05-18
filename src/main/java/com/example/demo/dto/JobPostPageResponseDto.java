package com.example.demo.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class JobPostPageResponseDto {

    private List<JobPostResponseDto> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int size;

    public JobPostPageResponseDto(
            List<JobPostResponseDto> posts,
            int currentPage,
            int totalPages,
            long totalElements,
            int size
    ) {
        this.content = posts;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.size = size;
    }
}