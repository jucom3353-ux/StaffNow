package com.example.demo.dto;

public class LoginResponseDto {

    private String accessToken;

    private String refreshToken;

    private String role;

    public LoginResponseDto(
            String accessToken,
            String refreshToken,
            String role
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getRole() {
        return role;
    }
}