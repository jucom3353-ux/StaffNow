package com.example.demo.dto;

import com.example.demo.entity.Portfolio;
import com.example.demo.entity.PortfolioImage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PortfolioResponseDto {

    private Long id;
    private Long userId;
    private String userName;
    private String categoryName;
    private String title;
    private String description;
    private int imageCount;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PortfolioResponseDto(Portfolio portfolio, List<PortfolioImage> images) {
        this.id = portfolio.getId();
        this.userId = portfolio.getUser().getId();
        this.userName = portfolio.getUser().getName();
        this.categoryName = portfolio.getCategory() != null
                ? portfolio.getCategory().getName() : null;
        this.title = portfolio.getTitle();
        this.description = portfolio.getDescription();
        this.imageCount = portfolio.getImageCount();
        this.imageUrls = images.stream()
                .map(PortfolioImage::getImageUrl)
                .collect(Collectors.toList());
        this.createdAt = portfolio.getCreatedAt();
        this.updatedAt = portfolio.getUpdatedAt();
    }
}