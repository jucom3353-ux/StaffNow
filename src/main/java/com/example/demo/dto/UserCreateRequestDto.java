package com.example.demo.dto;

public class UserCreateRequestDto {

    private String email;

    private String password;

    private String name;

    private String phone;

    private String role;

    private String mbti;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getMbti() {
        return mbti;
    }
}