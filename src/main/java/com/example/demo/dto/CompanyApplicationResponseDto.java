package com.example.demo.dto;

import com.example.demo.entity.*;
import java.time.LocalDateTime;

public class CompanyApplicationResponseDto {

    public Long id;
    public String status;
    public LocalDateTime createdAt;
    public Long workerId;
    public WorkSessionDto workSession;
    public WorkerDto worker;
    public ContractDto contract;
    public AttendanceDto attendance;

    public static class WorkSessionDto {
        public Long id;
        public String startTime;
        public String endTime;
        public int capacity;
        public int filledCount;
        public JobPostDto jobPost;
    }

    public static class JobPostDto {
        public Long id;
        public String title;
        public String deadline;
        public String status;
        public String workLocation;
        public Integer sortOrder;
    }

    public static class WorkerDto {
        public Long id;
        public String name;
        public String email;
        public String avatarUrl;
        public String address;
        public boolean isBoosted;
        public Double avgScore;
        public int reviewCount;
    }

    public static class ContractDto {
        public Long id;
        public String status;
    }

    public static class AttendanceDto {
        public Long id;
        public LocalDateTime checkInAt;
    }
}