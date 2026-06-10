package com.example.demo.dto;

import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConversationResponseDto {

    private Long userId;
    private String userName;
    private String companyName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;

    public ConversationResponseDto(User user, String lastMessage,
            LocalDateTime lastMessageTime, int unreadCount) {
        this.userId = user.getId();
        this.userName = user.getName();
        this.companyName = user.getCompanyName();
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }
}