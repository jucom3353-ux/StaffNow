package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Optional;


@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이름
    private String name;

    // 이메일
    @Column(unique = true)
    private String email;

    // 전화번호
    private String phone;

    // 비밀번호
    private String password;

    // 노쇼 횟수
    private int noShowCount;

    // 평점
    private double rating;

    // 사용자 역할(USER / COMPANY)
    @Enumerated(EnumType.STRING)
    private Role role;
    
}