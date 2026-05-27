package com.example.demo.dto;

import com.example.demo.entity.CompanyInviteCode;
import com.example.demo.entity.Role;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CompanyInviteCodeResponseDto {
    private Long id;
    private String code;
    private Role role;
    private boolean used;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    public CompanyInviteCodeResponseDto(CompanyInviteCode inviteCode) {
        this.id = inviteCode.getId();
        this.code = inviteCode.getCode();
        this.role = inviteCode.getRole();
        this.used = inviteCode.isUsed();
        this.expiredAt = inviteCode.getExpiredAt();
        this.createdAt = inviteCode.getCreatedAt();
    }
}