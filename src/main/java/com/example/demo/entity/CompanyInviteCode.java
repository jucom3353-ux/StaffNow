package com.example.demo.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_invite_codes")
@EntityListeners(AuditingEntityListener.class)
public class CompanyInviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private User company;

    @Column(unique = true, nullable = false, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MANAGER;

    @Column(nullable = false)
    private boolean used = false;

    @Column
    private LocalDateTime expiredAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public User getCompany() { return company; }
    public String getCode() { return code; }
    public Role getRole() { return role; }
    public boolean isUsed() { return used; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCompany(User company) { this.company = company; }
    public void setCode(String code) { this.code = code; }
    public void setRole(Role role) { this.role = role; }
    public void setUsed(boolean used) { this.used = used; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
}