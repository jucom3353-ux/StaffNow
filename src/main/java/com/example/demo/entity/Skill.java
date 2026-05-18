package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skill")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String name;        // 스킬명 (자유 입력)
    private String category;    // 카테고리 태그 (IT, 디자인, 운전, 언어 등)

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return name; }
    public String getCategory() { return category; }

    public void setUser(User user) { this.user = user; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
}