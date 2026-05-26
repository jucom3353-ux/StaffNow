package com.example.demo.dto;

import com.example.demo.entity.PostComment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostCommentResponseDto {

    private Long id;
    private Long userId;
    private String userName;
    private String content;
    private LocalDateTime createdAt;

    public PostCommentResponseDto(PostComment comment) {
        this.id = comment.getId();
        this.userId = comment.getUser().getId();
        this.userName = comment.getUser().getName();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
    }
}