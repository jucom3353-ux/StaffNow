package com.example.demo.dto;

import com.example.demo.entity.User;
import lombok.Getter;

@Getter
public class ConversationResponseDto {

    private Long userId;
    private String userName;
    private String companyName;

    public ConversationResponseDto(User user) {
        this.userId = user.getId();
        this.userName = user.getName();
        this.companyName = user.getCompanyName();
    }
}