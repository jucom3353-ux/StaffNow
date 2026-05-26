package com.example.demo.dto;

import com.example.demo.entity.PostCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDto {
    private PostCategory category;
    private String title;
    private String content;
}