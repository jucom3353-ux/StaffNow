package com.example.demo.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {

    private String accessToken;
    private String refreshToken;
    private String role;
    private String name;
    private String email;

    public LoginResponseDto(
            String accessToken,
            String refreshToken,
            String role,
            String name,
            String email
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.name = name;
        this.email = email;
    }
}