package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.demo.entity.Role;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateRequestDto {

    private String email;
    private String password;
    private String name;
    private String phone;
    private String companyName;
    private Role role;
    private String mbti;
}