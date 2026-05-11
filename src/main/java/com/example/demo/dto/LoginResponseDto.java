package com.example.demo.dto;

public class LoginResponseDto {

    private String accessToken;

    private String refreshToken;

    public LoginResponseDto(
            String accessToken,
            String refreshToken
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}