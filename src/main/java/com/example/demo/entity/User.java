package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private String email;

    // 전화번호
    private String phone;

    // 노쇼 횟수
    private int noShowCount;

    // 평점
    private double rating;

    // 분류
    private String role;
}