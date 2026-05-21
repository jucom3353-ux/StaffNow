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

    // 변경: String category → JobCategory FK
    @ManyToOne
    @JoinColumn(name = "category_id")
    private JobCategory category;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return name; }
    public JobCategory getCategory() { return category; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setName(String name) { this.name = name; }
    public void setCategory(JobCategory category) { this.category = category; }
}