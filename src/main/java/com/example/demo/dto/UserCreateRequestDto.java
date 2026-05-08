package com.example.demo.dto;

import com.example.demo.entity.Role;

public class UserCreateRequestDto {

    private String name;

    private String email;

    private String phone;

    private String password;

    // USER / COMPANY
    private Role role;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}