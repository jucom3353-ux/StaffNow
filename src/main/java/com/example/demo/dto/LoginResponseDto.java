package com.example.demo.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {

    private String role;
    private String name;
    private String email;
    private String phone;
    private String mbti;

    public LoginResponseDto(
            String role,
            String name,
            String email,
            String phone,
            String mbti
    ) {
        this.role = role;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.mbti = mbti;
    }
}