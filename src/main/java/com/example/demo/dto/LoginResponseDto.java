package com.example.demo.dto;

public class LoginResponseDto {

    private String accessToken;

    private String refreshToken;

    private String role;

    private String name;

    private String email;

    private String phone;

    private String mbti;

    public LoginResponseDto(
            String accessToken,
            String refreshToken,
            String role,
            String name,
            String email,
            String phone,
            String mbti
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.mbti = mbti;
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

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getMbti() {
        return mbti;
    }
}