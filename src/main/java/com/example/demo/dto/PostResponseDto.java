package com.example.demo.dto;

import com.example.demo.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponseDto {

    private Long id;
    private Long userId;
    private String userName;
    private String category;
    private String title;
    private String content;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.userId = post.getUser().getId();
        this.userName = post.getUser().getName();
        this.category = post.getCategory().name();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getComments() != null ? post.getComments().size() : 0;
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}