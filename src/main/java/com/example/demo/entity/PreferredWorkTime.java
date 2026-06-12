package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "preferred_work_time")
public class PreferredWorkTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "day_type")
    private String dayType;   // 평일, 주말, 요일무관

    @Column(name = "time_type")
    private String timeType;  // 새벽, 오전, 오후, 저녁, 시간무관

    private String startTime;
    private String endTime;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getDayType() { return dayType; }
    public String getTimeType() { return timeType; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    public void setUser(User user) { this.user = user; }
    public void setDayType(String dayType) { this.dayType = dayType; }
    public void setTimeType(String timeType) { this.timeType = timeType; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}