package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "preferred_work_time")
public class PreferredWorkTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String timeType; // 주말, 평일, 새벽, 오전, 오후, 저녁

    private String startTime; // nullable, 나중에 시간 추가될 때 사용
    private String endTime;   // nullable, 나중에 시간 추가될 때 사용

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getTimeType() { return timeType; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    public void setUser(User user) { this.user = user; }
    public void setTimeType(String timeType) { this.timeType = timeType; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}