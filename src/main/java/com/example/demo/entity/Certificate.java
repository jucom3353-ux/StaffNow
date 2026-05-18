package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "certificate")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;

    private String name;            // 자격증명
    private String issuer;          // 발급기관
    private String acquiredDate;    // 취득년월

    public Long getId() { return id; }
    public Resume getResume() { return resume; }
    public String getName() { return name; }
    public String getIssuer() { return issuer; }
    public String getAcquiredDate() { return acquiredDate; }

    public void setResume(Resume resume) { this.resume = resume; }
    public void setName(String name) { this.name = name; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public void setAcquiredDate(String acquiredDate) { this.acquiredDate = acquiredDate; }
}