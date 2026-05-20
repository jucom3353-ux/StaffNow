package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "job_category")
public class JobCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // 1=대분류, 2=중분류
    private Integer level;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private JobCategory parent;

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getLevel() { return level; }
    public JobCategory getParent() { return parent; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLevel(Integer level) { this.level = level; }
    public void setParent(JobCategory parent) { this.parent = parent; }
}