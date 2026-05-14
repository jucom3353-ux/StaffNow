package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateRequestDto {

    private String email;
    private String password;
    private String name;
    private String phone;
    private String companyName;
    private String role;
    private String mbti;
}
